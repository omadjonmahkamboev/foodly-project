# Foodly

Foodly is a multi-role Android food delivery app built with Kotlin and Jetpack Compose. The project includes a customer flow, courier workspace, admin dashboard, AI food assistant, Yandex map-based address selection, and an optional order sync backend for sharing live orders between devices.

## Highlights

- Customer app flow with onboarding, authentication, restaurant browsing, search, cart, checkout, order history, and profile screens
- Courier flow with sign up/log in, available orders, delivery status updates, earnings, reviews, and customer chat
- Admin dashboard for restaurant data, coupons, and delivery-related monitoring
- AI assistant screen focused on food, cart, menu ingredients, and order guidance
- Favorites, coupons, notifications, saved addresses, and saved payment cards
- Multi-language UI with English, Russian, and Uzbek content
- Local-first demo backend for auth, users, cards, favorites, and orders
- Optional HTTP sync server for sharing courier orders and chat between devices

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Credentials + Google Identity
- Yandex MapKit and Yandex geocoding/routing
- Coil 3
- SQLite via a local in-app backend layer
- Optional Java or Node.js sync backend

## Project Structure

```text
app/       Android application source
backend/   Optional order sync backend (Java and Node.js versions)
gradle/    Gradle version catalog and wrapper
```

## Requirements

- Android Studio with Android SDK 36
- JDK 11
- Android device or emulator with `minSdk 28`

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Add local configuration values to `local.properties`.
4. Sync Gradle and run the `app` configuration.

### local.properties

`local.properties` is ignored by git. Add the keys you need:

```properties
sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk

MAPKIT_API_KEY=your_yandex_mapkit_key
ROUTING_API_KEY=your_yandex_routing_key_optional
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id_optional
ORDER_SYNC_BASE_URL=http://YOUR_SERVER_IP_OR_DOMAIN:8080
```

Notes:

- `MAPKIT_API_KEY` is needed for Yandex map, reverse geocoding, and route features.
- `ROUTING_API_KEY` currently falls back to `MAPKIT_API_KEY` if omitted.
- `GOOGLE_WEB_CLIENT_ID` is only required for Google sign-in.
- `ORDER_SYNC_BASE_URL` is optional. If empty, the app stays in local demo mode and devices will not share courier orders with each other.

## Build

Debug APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

Unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Demo Access

Customer demo account:

```text
Email: hello@foodly.app
Password: 12345678
```

Admin demo access:

```text
Username: admin
Password: 123456789
```

Notes:

- Enter `admin` in the main auth screen to open the admin dashboard.
- Courier accounts can be created from the courier auth flow inside the app.

## Debug Development Notes

- The local in-app backend stores users, payment cards, and favorites in SQLite on the device.
- In debug builds, registration codes are exposed for development and verification emails are logged to Logcat with the tag `FoodlyEmail`.
- Password reset codes are also logged through the same debug email sender.
- If Yandex routing is unavailable during quote calculation, the backend falls back to a local distance estimate for delivery pricing.

## Order Sync Backend

The Android app can work fully in local demo mode, but shared live courier orders and chat require the sync backend in `backend/`.

### Run the Java version

```powershell
javac backend\OrderSyncServer.java
java -cp backend OrderSyncServer
```

The Java server listens on port `8080` by default and stores data in `backend/order-sync-data.json`.

Health check:

```text
http://localhost:8080/health
```

### Run the Node.js version

```powershell
node backend\order-sync-server.js
```

### Connect the app

Set the same public or local backend URL in `local.properties`:

```properties
ORDER_SYNC_BASE_URL=http://YOUR_SERVER_IP_OR_DOMAIN:8080
```

Then rebuild the APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

If customer and courier devices are on different networks, expose the server through a public VPS or a tunnel such as Cloudflare Tunnel or ngrok.

## Main Screens and Flows

- Splash, onboarding, sign in, sign up, password reset, and Google sign-in
- Delivery address setup with Yandex map picker
- Home, search, restaurant, menu details, cart, payment, and checkout
- Active orders, order tracking, and customer-courier chat
- Favorites, coupons, notifications, FAQ, settings, and profile editing
- Courier home, messages, reviews, profile, and delivery status workflow
- Admin dashboard for catalog and discount management
- AI assistant focused on food and ordering help

## Repository Notes

- `local.properties`, build folders, and local IDE files are excluded from git.
- The repository includes both the Android client and the optional backend helpers in one place.

## Author

Created by Omadjon Mahkamboev
