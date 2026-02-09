# Halo - Location Alarm App

Halo is a production-ready Android application that allows users to set location-based alarms. It uses Geofencing technology to trigger a full-screen alarm notification, sound, and vibration when the user enters a specified radius of a destination. The app is built with modern Android development practices, including Jetpack Compose and Material 3.

## 📱 Features

-   **Location-Based Alarms**: Set alarms by dropping a pin on a map.
-   **Customizable Radius**: Adjust the trigger radius from 100m to 5km.
-   **Background Monitoring**: Reliable geofencing even when the app is closed.
-   **High-Priority Alerts**: Full-screen notification with sound and vibration to wake you up.
-   **Modern UI**: Beautiful Material 3 design with Dark Mode and Dynamic Color support.
-   **Location Management**: Enable/Disable alarms easily from the home screen.

## 🛠 Tech Stack

-   **Language**: Kotlin
-   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
-   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
-   **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room)
-   **Maps & Location**:
    -   Google Maps SDK for Android
    -   Google Play Services Location (Geofencing API)
-   **Asynchronous Programming**: Coroutines & Flow
-   **Permissions**: Accompanist Permissions

## 🏗 Architecture Overview

## 🏗 Architecture Overview
30: 
31: The app follows the recommended **Modern Android Architecture** (MVVM) with a Unidirectional Data Flow (UDF).
32: 
33: ### 1. UI Layer (Presentation)
34: -   **Single Activity**: `MainActivity` is the sole entry point, hosting a generic `NavHost`.
35: -   **Jetpack Compose**: All UI is built declaratively.
36: -   **ViewModels**: Maintain state using `StateFlow`. UI components observe these flows via `collectAsState` to react to changes automatically.
37: -   **Developer Mode Map**: If no API key is present, the app automatically falls back to a specialized "Dev Mode" map to allow UI testing without Google Cloud billing.
38: 
39: ### 2. Domain & Logic Layer
40: -   **GeofenceManager**: A wrapper around `GeofencingClient`. It handles the complexity of adding/removing OS-level geofences.
41: -   **BroadcastReceiver**: `GeofenceBroadcastReceiver` listens for system intents when a user enters a monitored application. It acts as the bridge between the OS and the App's notification logic.
42: -   **Use Cases**: (Future scalability) Encapsulate specific business rules.
43: 
44: ### 3. Data Layer
45: -   **Repository Pattern**: `AlarmRepository` is the single source of truth. It coordinates between the local database and the geofencing system.
46: -   **Room Database**: Persists alarm data (Location, Name, Radius, Active State).
47: -   **DataStore**: Handles lightweight user preferences like "Dark Mode" settings.
48: 
49: ### 🔄 Key Data Flow: Alarm Trigger
50: 1.  User adds alarm -> `ViewModel` saves to `Repository`.
51: 2.  `Repository` saves to `Room` AND registers geofence via `GeofenceManager`.
52: 3.  OS tracks location (even if app is killed).
53: 4.  User enters radius -> OS fires `Intent`.
54: 5.  `GeofenceBroadcastReceiver` wakes up -> Triggers Notification/Sound.

## 🚀 Setup & Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/halo.git
    ```

2.  **Add Google Maps API Key**:
    -   Get an API Key from the [Google Cloud Console](https://console.cloud.google.com/).
    -   Enable **Maps SDK for Android**.
    -   Open `app/src/main/AndroidManifest.xml`.
    -   Replace `YOUR_API_KEY` with your actual key:
        ```xml
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_ACTUAL_API_KEY_HERE" />
        ```

3.  **Build and Run**:
    -   Open the project in Android Studio Iguana or later.
    -   Sync Gradle files.
    -   Run on a device or emulator.

    > **Note**: For Geofencing to work on an emulator, you must manually simulate location updates in the emulator settings (Extended Controls > Location).

## ⚠️ Permissions

The app requires the following permissions to function correctly:
-   **Location**: `ACCESS_FINE_LOCATION` and `ACCESS_BACKGROUND_LOCATION` to track location updates.
-   **Notifications**: `POST_NOTIFICATIONS` to show alerts.
-   **Overlays**: Full-screen intent permission for the alarm trigger.

Permissions are requested at runtime via the Settings screen or when necessary.

## 🧪 Testing

-   **Manual Verification**:
    1.  Grant all permissions.
    2.  Add an alarm at a location ~500m away.
    3.  Simulate movement into that area.
    4.  Verify the alarm triggers.

## 📄 License

```text
Copyright 2024 Halo Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
