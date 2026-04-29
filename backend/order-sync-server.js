const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = Number(process.env.PORT || 8080);
const DATA_FILE = process.env.DATA_FILE || path.join(__dirname, "order-sync-data.json");

let state = {
  orders: [],
  messages: [],
};

loadState();

const server = http.createServer(async (request, response) => {
  setCorsHeaders(response);

  if (request.method === "OPTIONS") {
    response.writeHead(204);
    response.end();
    return;
  }

  const url = new URL(request.url, `http://${request.headers.host || "localhost"}`);
  const parts = url.pathname.split("/").filter(Boolean);

  try {
    if (request.method === "GET" && url.pathname === "/health") {
      sendJson(response, 200, { ok: true });
      return;
    }

    if (request.method === "GET" && url.pathname === "/snapshot") {
      sendJson(response, 200, state);
      return;
    }

    if (request.method === "POST" && url.pathname === "/orders") {
      const order = await readJson(request);
      if (!order.id) {
        sendJson(response, 400, { error: "Order id is required" });
        return;
      }
      upsertById(state.orders, order);
      saveState();
      sendJson(response, 200, { ok: true });
      return;
    }

    if (request.method === "POST" && parts.length === 3 && parts[0] === "orders" && parts[2] === "status") {
      const orderId = decodeURIComponent(parts[1]);
      const patch = await readJson(request);
      const index = state.orders.findIndex((order) => order.id === orderId);
      if (index < 0) {
        sendJson(response, 404, { error: "Order not found" });
        return;
      }
      state.orders[index] = {
        ...state.orders[index],
        status: patch.status || state.orders[index].status,
        courierId: Object.prototype.hasOwnProperty.call(patch, "courierId") ? patch.courierId : state.orders[index].courierId || null,
        courierPoint: Object.prototype.hasOwnProperty.call(patch, "courierPoint") ? patch.courierPoint : state.orders[index].courierPoint || null,
      };
      saveState();
      sendJson(response, 200, { ok: true });
      return;
    }

    if (request.method === "POST" && url.pathname === "/messages") {
      const message = await readJson(request);
      if (!message.id || !message.orderId) {
        sendJson(response, 400, { error: "Message id and orderId are required" });
        return;
      }
      upsertById(state.messages, message);
      saveState();
      sendJson(response, 200, { ok: true });
      return;
    }

    sendJson(response, 404, { error: "Not found" });
  } catch (error) {
    sendJson(response, 500, { error: error.message || "Server error" });
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Foodly order sync server is running on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
});

function loadState() {
  if (!fs.existsSync(DATA_FILE)) return;
  try {
    const data = JSON.parse(fs.readFileSync(DATA_FILE, "utf8"));
    state = {
      orders: Array.isArray(data.orders) ? data.orders : [],
      messages: Array.isArray(data.messages) ? data.messages : [],
    };
  } catch (error) {
    console.warn(`Could not read ${DATA_FILE}: ${error.message}`);
  }
}

function saveState() {
  fs.mkdirSync(path.dirname(DATA_FILE), { recursive: true });
  const tempFile = `${DATA_FILE}.tmp`;
  fs.writeFileSync(tempFile, JSON.stringify(state, null, 2));
  fs.renameSync(tempFile, DATA_FILE);
}

function upsertById(items, nextItem) {
  const index = items.findIndex((item) => item.id === nextItem.id);
  if (index >= 0) {
    items[index] = { ...items[index], ...nextItem };
  } else {
    items.unshift(nextItem);
  }
}

function readJson(request) {
  return new Promise((resolve, reject) => {
    let body = "";
    request.on("data", (chunk) => {
      body += chunk;
      if (body.length > 1024 * 1024) {
        request.destroy(new Error("Request body is too large"));
      }
    });
    request.on("end", () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (error) {
        reject(new Error("Invalid JSON"));
      }
    });
    request.on("error", reject);
  });
}

function sendJson(response, statusCode, data) {
  response.writeHead(statusCode, { "Content-Type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(data));
}

function setCorsHeaders(response) {
  response.setHeader("Access-Control-Allow-Origin", "*");
  response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
  response.setHeader("Access-Control-Allow-Headers", "Content-Type");
}
