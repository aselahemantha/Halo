# Halo - Location Alarm App

Halo is a production-ready Android application that allows users to set location-based alarms. It uses Geofencing technology to trigger a full-screen alarm notification, sound, and vibration when the user interacts with a specified radius of a destination. Built with modern Android development practices like Jetpack Compose, Material 3, Hilt, and Room, Halo makes sure you never miss your stop.

## 📱 Features

-   **Location-Based Alarms**: Set up precise alarms by dropping a pin on an interactive map.
-   **Configurable Triggers**: Choose whether the alarm triggers when you **Arrive** (Enter), **Leave** (Exit), or **Stay** (Dwell) at a location.
-   **Customizable Radius**: Adjust the trigger radius smoothly from 100m up to 5km.
-   **Schedule & Repetition**: Set specific days of the week and time windows (e.g., only trigger between 8 AM - 5 PM) for each alarm to be active.
-   **Home Screen Widget**: View active alarm count and quickly access the Add Alarm screen using the Jetpack Glance widget.
-   **Backup & Restore**: Easily export your alarms to a JSON file and import them back on any device.
-   **Share Alarm**: Share your alarm configuration with others via a generated QR code or a `halo://alarm` deep link.
-   **Background Monitoring**: Reliable geofencing using Android Location Services, even when the app is closed.
-   **Auto-Resume on Boot**: WorkManager ensures your active alarms are seamlessly re-registered after a device restart.
-   **High-Priority Alerts**: Full-screen notifications with custom notification channels, sound, and vibration patterns to ensure you wake up.
-   **Localization (i18n)**: Fully externalized string resources ready for multi-language support.
-   **Modern UI**: Beautiful Jetpack Compose UI with Material 3, supporting light and dark themes.

## 🛠 Tech Stack

-   **Language**: Kotlin
-   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
-   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
-   **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) & Preferences DataStore
-   **Maps & Location**:
    -   Google Maps SDK for Android
    -   Google Play Services Location (Geofencing API, FusedLocationProviderClient)
-   **Background Processing**: WorkManager (for Boot Re-registration)
-   **Widgets**: Jetpack Glance
-   **Asynchronous Programming**: Coroutines & Flow
-   **Permissions**: Accompanist Permissions

## 🏗 Architecture Overview

The app follows the recommended **Modern Android Architecture** (MVVM) with a Unidirectional Data Flow (UDF).

### 1. UI Layer (Presentation)
-   **Single Activity**: `MainActivity` is the sole entry point, hosting a generic Jetpack Navigation `NavHost`.
-   **Jetpack Compose**: All UI interfaces, including complex bottom sheets and settings screens, are built declaratively.
-   **ViewModels**: Maintain robust state using `StateFlow`. UI components observe these flows to react to state changes automatically and handle process death using `SavedStateHandle`.
-   **Glance Widget**: The widget UI (`HaloWidget`) and behavior are decoupled, observing the Room DB directly via an entry point logic.

### 2. Domain & Logic Layer
-   **GeofenceManager**: A strategic wrapper around `GeofencingClient`. It bridges the domain models directly into Android OS geofencing boundaries, managing enter, exit, and dwell transition types.
-   **BroadcastReceiver**: `GeofenceBroadcastReceiver` listens for system intents when a user crosses a monitored spatial boundary. It delegates to the `LocationForegroundService` to surface alerts.

### 3. Data Layer
-   **Repository Pattern**: `AlarmRepository` acts as the single source of truth coordinating local database inputs and providing Kotlin Flows for reactive observation.
-   **Room Database**: Persists complex alarm entities including coordinates, radii, triggers, and schedule structures.
-   **Serialization**: GSON integration built-in to support exporting/importing Room data structures seamlessly.

### 🔄 Key Data Flow: Alarm Trigger
1.  User configures an alarm in the UI -> `ViewModel` passes data to `Repository`.
2.  `Repository` saves the entity to `Room` AND registers the hardware-level geofence via `GeofenceManager`.
3.  OS tracks location accurately via Google Play Services in the background.
4.  User interacts with the radius (Enters/Exits/Stays) -> OS broadcasts a `GeofencingEvent` Intent.
5.  `GeofenceBroadcastReceiver` wakes the app and triggers `LocationForegroundService`.
6.  Service emits High-Priority Notification, launches a full-screen Intent, and plays sound.

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

## ⚠️ Permissions requirements

The app requires the following Android permissions to guarantee full operational reliability:
-   **Location**: `ACCESS_FINE_LOCATION` and `ACCESS_BACKGROUND_LOCATION` to reliably track device movement.
-   **Notifications**: `POST_NOTIFICATIONS` to show heads-up and lock screen alerts.
-   **System Overlays**: `USE_FULL_SCREEN_INTENT` designed specifically for alarm-clock style waking screens.

Permissions are requested gracefully at runtime via the Settings "PERMISSIONS" dashboard.

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
