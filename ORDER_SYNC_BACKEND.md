# Foodly Order Sync Backend

The Android app keeps login/profile data locally, but live courier orders, order statuses, and chat messages can be shared through a small HTTP sync server.

## Run the demo server with Java

From the project root:

```powershell
javac backend\OrderSyncServer.java
java -cp backend OrderSyncServer
```

The server listens on port `8080` by default and stores data in `backend/order-sync-data.json`.

Health check:

```text
http://localhost:8080/health
```

## Connect the Android app

Add the public server URL to `local.properties` before building the APK:

```properties
ORDER_SYNC_BASE_URL=http://YOUR_SERVER_IP_OR_DOMAIN:8080
```

Then rebuild and install/send the new APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

Both the customer APK and courier APK must be built with the same `ORDER_SYNC_BASE_URL`. If the URL is empty, the app falls back to local demo mode and devices will not see each other's orders.

## Optional Node version

There is also a Node version at `backend/order-sync-server.js`:

```powershell
node backend\order-sync-server.js
```

## Public internet note

If the customer and courier are on different networks, use a public VPS/domain or a tunnel such as Cloudflare Tunnel/ngrok and put that public URL into `ORDER_SYNC_BASE_URL`.
