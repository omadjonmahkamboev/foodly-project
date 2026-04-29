import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OrderSyncServer {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final Path DATA_FILE = Paths.get(
        System.getenv().getOrDefault("DATA_FILE", "backend/order-sync-data.json")
    );
    private static final LinkedHashMap<String, String> orders = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> messages = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        loadState();

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", OrderSyncServer::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Foodly order sync server is running on port " + PORT);
        System.out.println("Health check: http://localhost:" + PORT + "/health");
    }

    private static void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        String method = exchange.getRequestMethod();
        if ("OPTIONS".equals(method)) {
            sendJson(exchange, 204, "");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        try {
            if ("GET".equals(method) && "/health".equals(path)) {
                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            if ("GET".equals(method) && "/snapshot".equals(path)) {
                sendJson(exchange, 200, snapshotJson());
                return;
            }

            if ("POST".equals(method) && "/orders".equals(path)) {
                String body = readBody(exchange);
                String id = extractString(body, "id");
                if (id == null || id.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Order id is required\"}");
                    return;
                }
                upsertFirst(orders, id, body);
                saveState();
                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            if ("POST".equals(method) && parts.length == 4 && "orders".equals(parts[1]) && "status".equals(parts[3])) {
                String orderId = URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
                String existing = orders.get(orderId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Order not found\"}");
                    return;
                }

                String body = readBody(exchange);
                String status = extractString(body, "status");
                String courierId = extractNullableString(body, "courierId");
                String courierPoint = extractJsonValue(body, "courierPoint");
                String updated = existing;
                if (status != null && !status.isBlank()) {
                    updated = replaceJsonValue(updated, "status", quote(status));
                }
                updated = replaceJsonValue(updated, "courierId", courierId == null ? "null" : quote(courierId));
                if (courierPoint != null) {
                    updated = replaceJsonValue(updated, "courierPoint", courierPoint);
                }
                upsertFirst(orders, orderId, updated);
                saveState();
                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            if ("POST".equals(method) && "/messages".equals(path)) {
                String body = readBody(exchange);
                String id = extractString(body, "id");
                String orderId = extractString(body, "orderId");
                if (id == null || id.isBlank() || orderId == null || orderId.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Message id and orderId are required\"}");
                    return;
                }
                upsertFirst(messages, id, body);
                saveState();
                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            sendJson(exchange, 404, "{\"error\":\"Not found\"}");
        } catch (Exception error) {
            sendJson(exchange, 500, "{\"error\":" + quote(error.getMessage()) + "}");
        }
    }

    private static void loadState() {
        if (!Files.exists(DATA_FILE)) return;

        try {
            String json = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
            for (String order : arrayObjects(json, "orders")) {
                String id = extractString(order, "id");
                if (id != null) orders.put(id, order);
            }
            for (String message : arrayObjects(json, "messages")) {
                String id = extractString(message, "id");
                if (id != null) messages.put(id, message);
            }
        } catch (Exception error) {
            System.out.println("Could not read " + DATA_FILE + ": " + error.getMessage());
        }
    }

    private static void saveState() throws IOException {
        Files.createDirectories(DATA_FILE.getParent());
        Path tempFile = Paths.get(DATA_FILE.toString() + ".tmp");
        Files.writeString(tempFile, snapshotJson(), StandardCharsets.UTF_8);
        Files.move(tempFile, DATA_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static String snapshotJson() {
        return "{\"orders\":[" + String.join(",", orders.values()) + "],\"messages\":["
            + String.join(",", messages.values()) + "]}";
    }

    private static void upsertFirst(LinkedHashMap<String, String> map, String key, String value) {
        LinkedHashMap<String, String> next = new LinkedHashMap<>();
        next.put(key, value);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!entry.getKey().equals(key)) {
                next.put(entry.getKey(), entry.getValue());
            }
        }
        map.clear();
        map.putAll(next);
    }

    private static List<String> arrayObjects(String json, String arrayName) {
        int keyIndex = json.indexOf("\"" + arrayName + "\"");
        if (keyIndex < 0) return List.of();

        int start = json.indexOf('[', keyIndex);
        if (start < 0) return List.of();

        int end = findMatching(json, start, '[', ']');
        if (end <= start) return List.of();

        return splitObjects(json.substring(start + 1, end));
    }

    private static List<String> splitObjects(String body) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int index = 0; index < body.length(); index++) {
            char current = body.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (current == '{') {
                if (depth == 0) start = index;
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(body.substring(start, index + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private static int findMatching(String text, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int index = start; index < text.length(); index++) {
            char current = text.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) return index;
            }
        }
        return -1;
    }

    private static String extractString(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
            .matcher(json);
        return matcher.find() ? unescape(matcher.group(1)) : null;
    }

    private static String extractNullableString(String json, String name) {
        if (Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*null").matcher(json).find()) {
            return null;
        }
        return extractString(json, name);
    }

    private static String extractJsonValue(String json, String name) {
        Matcher matcher = Pattern.compile(
            "\"" + Pattern.quote(name) + "\"\\s*:\\s*(\\{[^{}]*\\}|\"(?:\\\\.|[^\"])*\"|null|true|false|-?\\d+(?:\\.\\d+)?)"
        ).matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String replaceJsonValue(String json, String name, String replacementValue) {
        Pattern pattern = Pattern.compile(
            "\"" + Pattern.quote(name) + "\"\\s*:\\s*(\\{[^{}]*\\}|\"(?:\\\\.|[^\"])*\"|null|true|false|-?\\d+(?:\\.\\d+)?)"
        );
        Matcher matcher = pattern.matcher(json);
        String field = "\"" + name + "\":" + replacementValue;
        if (matcher.find()) {
            return matcher.replaceFirst(Matcher.quoteReplacement(field));
        }
        int insertAt = json.lastIndexOf('}');
        if (insertAt < 0) return json;
        String prefix = json.substring(0, insertAt).trim();
        String separator = prefix.endsWith("{") ? "" : ",";
        return json.substring(0, insertAt) + separator + field + json.substring(insertAt);
    }

    private static String quote(String value) {
        if (value == null) return "null";
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            + "\"";
    }

    private static String unescape(String value) {
        return value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
