# Scheduled App Launcher for Android

This Android app allows users to **schedule installed apps to launch at specific times**, with support for modern Android background restrictions.

---

## Features

-  **Schedule App Launch**
    - Pick any installed app and schedule it to launch at a specific time.
    - Allows multiple schedules for the same app (as long as times don’t conflict).

-  **Notification-based Launch**
    - On Android 10+ (API 29+), the app shows a notification at the scheduled time. Tapping it launches the target app.
    - On Android 9 and below, the app launches directly at the scheduled time.

- **Schedule Management**
    - View all scheduled apps in a list.
    - Delete schedules that haven’t started yet.
    - Clear the full schedule list with one tap.

- **Modern UI**
    - Tabbed layout using ViewPager:
        - App List
        - Schedule List
        - Launched Apps (History)

- **Data Persistence**
    - Uses Room Database to store schedule info and launch logs locally.

---

## Compatibility

- **Minimum SDK:** `26` (Android 8.0)
- **Target SDK:** `35` (Android 14)

| Android Version        | Launch Behavior                         |
|------------------------|------------------------------------------|
| Android 9 and below    | App launches directly                    |
| Android 10 and above   | Notification appears; user must tap it   |

---

## Tech Stack

-  **Kotlin**
-  **Room Database**
-  **AlarmManager + BroadcastReceiver**
-  **Material Design Components**
-  MVVM-ready architecture

