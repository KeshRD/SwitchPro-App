# 🔌 SWITCH PRO

A Bluetooth-controlled IoT switch controller for Android that communicates with an Arduino-based device through an **HC-05 Bluetooth module**. The application supports manual ON/OFF control, automatic mode, temperature-threshold configuration, and scheduled switching.

> **Package:** `com.kairaxus.switchpro`
> **App Name:** SWITCH PRO
> **Platform:** Android 7.0+ (API 24+)
> **Bluetooth:** HC-05 / Bluetooth SPP

---

## 📱 Overview

**SWITCH PRO** is an Android application designed to control an Arduino-based IoT switching system wirelessly.

The Android application communicates with an **HC-05 Bluetooth module** using the Bluetooth Serial Port Profile (SPP). Users can manually control the connected device, configure automatic temperature-based behavior, and schedule ON/OFF operations for specific dates and times.

### Main Features

* 🔵 Connect to an HC-05 Bluetooth module
* 🟢 Manually turn the device **ON**
* 🔴 Manually turn the device **OFF**
* 🤖 Enable **AUTO** mode
* 🌡️ Configure a temperature threshold
* ⏰ Schedule automatic ON/OFF operations
* 📅 Select specific dates and times for schedules
* 🗑️ Delete saved schedules
* 💾 Persist schedules using `SharedPreferences`
* ⏱️ Execute schedules using Android `AlarmManager`
* 📡 Communicate with Arduino through Bluetooth SPP

---

## 🏗️ Architecture

```text
┌─────────────────────────────┐
│       Android App           │
│        SWITCH PRO           │
│                             │
│  ┌───────────────────────┐  │
│  │     MainActivity      │  │
│  │                       │  │
│  │  ON / OFF / AUTO      │  │
│  │  Temperature Control  │  │
│  └───────────┬───────────┘  │
│              │              │
│  ┌───────────▼───────────┐  │
│  │   BluetoothHelper     │  │
│  │                       │  │
│  │   Bluetooth Socket    │  │
│  └───────────┬───────────┘  │
│              │              │
│        Bluetooth SPP        │
│              │              │
└──────────────┼──────────────┘
               │
               ▼
        ┌───────────────┐
        │     HC-05     │
        │    Module     │
        └───────┬───────┘
                │
                │ Serial
                ▼
        ┌───────────────┐
        │    Arduino    │
        │               │
        │  IoT Switch   │
        └───────────────┘
```

### Scheduling Architecture

```text
User
 │
 ▼
ScheduleActivity
 │
 ├── Select Date
 ├── Select Time
 └── Select ON/OFF
 │
 ▼
Schedule Object
 │
 ▼
Gson Serialization
 │
 ▼
SharedPreferences
 │
 └──────────────► AlarmManager
                       │
                       ▼
               ScheduleReceiver
                       │
                       ▼
                BluetoothHelper
                       │
                       ▼
                    HC-05
                       │
                       ▼
                   Arduino
```

---

## 🛠️ Tech Stack

| Technology          |     Version | Purpose                               |
| ------------------- | ----------: | ------------------------------------- |
| Kotlin              |       2.0.0 | Primary programming language          |
| Android SDK         |          35 | Android development                   |
| Min SDK             |          24 | Android 7.0+                          |
| Target SDK          |          35 | Target Android version                |
| AGP                 |       8.8.2 | Android build system                  |
| Java                |          11 | JVM source/target                     |
| AndroidX AppCompat  |       1.7.1 | Activity and compatibility support    |
| Material Components |      1.11.0 | XML UI components and theming         |
| CardView            |       1.0.0 | Card-based UI                         |
| RecyclerView        |       1.3.2 | Declared dependency; currently unused |
| Gson                |      2.10.1 | Schedule JSON serialization           |
| SharedPreferences   | Android SDK | Local schedule storage                |
| AlarmManager        | Android SDK | Scheduled task execution              |
| Bluetooth SPP       |       HC-05 | Arduino communication                 |

### UI Technology

The project contains Jetpack Compose dependencies and theme files, but the actual application UI is implemented using **traditional XML layouts**.

```text
XML Layouts
    │
    ├── activity_main.xml
    ├── activity_schedule.xml
    └── item_schedule.xml
```

Jetpack Compose is currently not used for the application's actual UI.

---

## 📂 Project Structure

```text
SWITCHPRO/
│
├── app/
│   └── src/
│       └── main/
│           │
│           ├── AndroidManifest.xml
│           │
│           ├── java/
│           │   └── com/kairaxus/switchpro/
│           │       │
│           │       ├── MainActivity.kt
│           │       ├── ScheduleActivity.kt
│           │       ├── Schedule.kt
│           │       ├── ScheduleAdapter.kt
│           │       ├── ScheduleReceiver.kt
│           │       ├── BluetoothHelper.kt
│           │       │
│           │       └── ui/theme/
│           │           ├── Color.kt
│           │           ├── Theme.kt
│           │           └── Type.kt
│           │
│           └── res/
│               │
│               ├── layout/
│               │   ├── activity_main.xml
│               │   ├── activity_schedule.xml
│               │   └── item_schedule.xml
│               │
│               ├── drawable/
│               │   ├── alarm.png
│               │   ├── auto.png
│               │   ├── backarr.png
│               │   ├── control.png
│               │   ├── k123321.png
│               │   ├── on.png
│               │   └── time.png
│               │
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   ├── styles.xml
│               │   └── themes.xml
│               │
│               └── xml/
│                   ├── backup_rules.xml
│                   └── data_extraction_rules.xml
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

---

# ⚙️ Core Components

## 1. MainActivity

`MainActivity` is the main control panel of the application.

### Responsibilities

* Connect to the paired HC-05 Bluetooth module
* Send manual ON/OFF commands
* Enable AUTO mode
* Open the schedule manager
* Send temperature threshold commands
* Previously supported timer-based schedule monitoring

### Available Controls

```text
CONNECT
   │
   ▼
HC-05 Bluetooth Device

ON
 │
 └──► "ON"

OFF
 │
 └──► "OFF"

AUTO
 │
 └──► "AUTO"

Temperature Threshold
 │
 └──► "TEMP:{value}"
```

---

## 2. ScheduleActivity

`ScheduleActivity` provides the schedule management interface.

Users can:

1. Select a date
2. Select a time
3. Select an ON/OFF command
4. Save the schedule
5. View saved schedules
6. Delete schedules

When a schedule is saved, it is stored locally and an exact Android alarm is created.

---

## 3. Schedule

The `Schedule` data class represents a scheduled operation.

```kotlin
data class Schedule(
    val datetime: String,
    val command: String
)
```

Example:

```json
{
  "datetime": "2026-08-14T20:30",
  "command": "ON"
}
```

---

## 4. ScheduleAdapter

`ScheduleAdapter` is a custom `BaseAdapter` used with a `ListView`.

It is responsible for:

* Displaying saved schedules
* Showing schedule date/time
* Showing ON/OFF command
* Providing a delete button

Example display:

```text
2026-08-14T20:30 - ON
2026-08-15T07:00 - OFF
```

---

## 5. BluetoothHelper

`BluetoothHelper` is a Kotlin singleton responsible for sharing the Bluetooth socket and sending commands.

```kotlin
object BluetoothHelper {
    var bluetoothSocket: BluetoothSocket? = null

    fun sendCommand(command: String) {
        // Send command through Bluetooth
    }
}
```

This allows components such as `ScheduleReceiver` to send commands through the existing Bluetooth connection.

---

## 6. ScheduleReceiver

`ScheduleReceiver` is an Android `BroadcastReceiver`.

When an `AlarmManager` alarm is triggered:

```text
AlarmManager
     │
     ▼
ScheduleReceiver
     │
     ▼
Read command
     │
     ▼
BluetoothHelper
     │
     ▼
HC-05
     │
     ▼
Arduino
```

For example:

```text
Scheduled time reached
        ↓
ScheduleReceiver
        ↓
"ON"
        ↓
BluetoothHelper
        ↓
HC-05
        ↓
Arduino
```

---

# 📡 Bluetooth Communication Protocol

The application uses the standard Bluetooth Serial Port Profile UUID:

```text
00001101-0000-1000-8000-00805F9B34FB
```

### Commands

| Command        | Description                   |
| -------------- | ----------------------------- |
| `ON`           | Turn the connected device ON  |
| `OFF`          | Turn the connected device OFF |
| `AUTO`         | Enable automatic mode         |
| `TEMP:{value}` | Set temperature threshold     |

### Example

```text
Android App
    │
    │ "ON"
    ▼
   HC-05
    │
    │ Serial
    ▼
 Arduino
    │
    ▼
Switch ON
```

Temperature example:

```text
TEMP:28
```

This can be interpreted by the Arduino as a temperature threshold of **28°C**, depending on the Arduino-side implementation.

---

# 💾 Data Storage

Schedules are stored locally using Android `SharedPreferences`.

### Preference File

```text
SCHEDULES
```

### Key

```text
schedule_list
```

### Example Stored Data

```json
[
  {
    "datetime": "2026-08-14T20:30",
    "command": "ON"
  },
  {
    "datetime": "2026-08-15T07:00",
    "command": "OFF"
  }
]
```

Gson is used to serialize and deserialize the schedule list.

---

# ⏰ Scheduling System

The application uses Android `AlarmManager` to execute scheduled operations.

When the user saves a schedule:

```text
Create Schedule
      ↓
Save to SharedPreferences
      ↓
Create AlarmManager alarm
      ↓
Wait until scheduled time
      ↓
ScheduleReceiver.onReceive()
      ↓
Send Bluetooth command
```

`setExactAndAllowWhileIdle()` is used so that scheduled operations can execute accurately even when the device is in an idle/doze state, subject to Android's alarm and permission policies.

---

# 🔐 Required Permissions

The application requires Bluetooth and alarm-related permissions.

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

### Android 12+

Android 12 (API 31) and newer devices require the appropriate Bluetooth runtime permissions, particularly:

```text
BLUETOOTH_SCAN
BLUETOOTH_CONNECT
```

The application therefore requests Bluetooth permissions at runtime where required.

---

# 🔧 Hardware Requirements

The Android application is designed to work with an Arduino-based IoT switching system.

### Typical Hardware

* Arduino board
* HC-05 Bluetooth module
* Relay / electronic switch
* Temperature sensor (for AUTO mode)
* Controlled electrical load
* Power supply
* Android smartphone

### Basic Hardware Architecture

```text
             ┌───────────────┐
             │ Android Phone │
             └───────┬───────┘
                     │
                Bluetooth
                     │
                     ▼
             ┌───────────────┐
             │     HC-05     │
             └───────┬───────┘
                     │ UART
                     ▼
             ┌───────────────┐
             │    Arduino    │
             └───────┬───────┘
                     │
             ┌───────┴───────┐
             ▼               ▼
          Relay          Temperature
          Control           Sensor
```

> **Safety:** If the Arduino/relay system controls mains voltage, use appropriate electrical isolation, protection, and qualified supervision. Do not work directly with mains wiring unless you are properly qualified to do so.

---

# 🚀 Getting Started

## Prerequisites

Install:

* Android Studio
* Android SDK 35
* JDK 11
* A physical Android device with Bluetooth support
* Arduino + HC-05 hardware for full functionality

---

## 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/SWITCHPRO.git
```

Then open the project in Android Studio.

---

## 2. Build the Project

Allow Android Studio to synchronize the Gradle dependencies.

Then build the application:

```bash
./gradlew assembleDebug
```

On Windows:

```bash
gradlew.bat assembleDebug
```

---

## 3. Install the Application

Connect an Android device with USB debugging enabled and run:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Alternatively, simply press **Run ▶** in Android Studio.

---

# 🔵 Connecting the HC-05

Before using the application:

1. Power on the HC-05 module.
2. Pair the Android phone with the HC-05 through Android Bluetooth settings.
3. Open SWITCH PRO.
4. Tap **Connect to Device**.
5. The application searches for the paired device named:

```text
HC-05
```

6. Once connected, the application can send commands to the Arduino.

---

# 🎮 Usage

## Manual Control

### Turn ON

Press:

```text
ON
```

The application sends:

```text
ON
```

to the Arduino.

### Turn OFF

Press:

```text
OFF
```

The application sends:

```text
OFF
```

### Automatic Mode

Press:

```text
AUTO
```

The application sends:

```text
AUTO
```

The Arduino can then handle automatic behavior according to its programmed logic.

---

# 🌡️ Temperature Control

Enter a temperature threshold, for example:

```text
28
```

Then press the temperature control button.

The intended Bluetooth command is:

```text
TEMP:28
```

The Arduino can use this value as the configured temperature threshold.

---

# ⏰ Creating a Schedule

1. Open **Timetable / Schedule**.
2. Select a date.
3. Select a time.
4. Choose `ON` or `OFF`.
5. Save the schedule.
6. The schedule is stored locally.
7. An Android alarm is created.
8. At the scheduled time, `ScheduleReceiver` sends the corresponding command.

Example:

```text
Date:    2026-08-14
Time:    20:30
Command: ON
```

Result:

```text
2026-08-14 20:30
       ↓
AlarmManager
       ↓
ScheduleReceiver
       ↓
"ON"
       ↓
HC-05
       ↓
Arduino
```

---

# 🐛 Known Issues

The current version contains several known issues that should be addressed before production deployment.

### 1. Temperature Command Bug

The temperature feature currently uses an `outputStream` field that is not initialized correctly.

As a result, the temperature threshold command may not actually be transmitted.

**Recommended fix:**

Use the existing `BluetoothHelper.sendCommand()` mechanism consistently, or initialize the output stream when the Bluetooth connection is established.

---

### 2. Incorrect Manifest Declarations

The following classes are not Android activities:

```text
BluetoothHelper
ScheduleAdapter
ScheduleReceiver
```

`BluetoothHelper` is a Kotlin singleton and `ScheduleAdapter` is a `BaseAdapter`.

They should not be declared using `<activity>` entries in `AndroidManifest.xml`.

`ScheduleReceiver` should only be registered as a `<receiver>`.

---

### 3. Duplicate ScheduleReceiver Declaration

`ScheduleReceiver` is currently declared both as an activity and receiver.

The unnecessary `<activity>` declaration should be removed.

---

### 4. Schedule Date/Time Selection

The time picker currently depends on `selectedDateTime` already being initialized.

If the user attempts to select a time before selecting a date, the resulting value may not be handled correctly.

A safer implementation should initialize the date/time state independently.

---

### 5. Activity Lifecycle / Permission Flow

Permission checking should be handled after `super.onCreate()` and through the Android activity permission lifecycle.

This makes the activity lifecycle more reliable when permissions are denied or requested.

---

### 6. Unused Jetpack Compose Dependencies

Compose dependencies and theme files are currently present, but the actual application UI is XML-based.

They can be removed if Compose is not planned for future development.

---

### 7. Unused RecyclerView Dependency

The project declares RecyclerView but currently uses:

```text
ListView + BaseAdapter
```

instead.

The dependency can either be removed or the schedule list can be migrated to RecyclerView.

---

### 8. App Label

The current application label appears as:

```text
Sgy
```

The intended application name is:

```text
SWITCH PRO
```

The label should be updated in `strings.xml`.

---

# 🔄 Schedule Execution Consideration

The application currently contains two scheduling approaches:

### Timer-Based Monitoring

```text
AUTO
 ↓
Timer
 ↓
Every 60 seconds
 ↓
Read SharedPreferences
 ↓
Check current time
 ↓
Execute matching command
```

### AlarmManager

```text
Save Schedule
 ↓
AlarmManager
 ↓
Exact scheduled time
 ↓
ScheduleReceiver
 ↓
Bluetooth command
```

The **AlarmManager approach is recommended as the primary scheduling mechanism** because it is designed for scheduled execution independently of the application's normal foreground UI lifecycle.

The timer-based polling mechanism can be removed or retained only if there is a specific requirement for continuous in-app monitoring.

---

# 🧩 Architecture Improvements

A future version could use a cleaner architecture such as:

```text
                    ┌───────────────────┐
                    │       UI          │
                    │ XML / Compose     │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │    ViewModel      │
                    └─────────┬─────────┘
                              │
                 ┌────────────┴────────────┐
                 ▼                         ▼
        ┌─────────────────┐       ┌─────────────────┐
        │ Bluetooth       │       │ Schedule        │
        │ Repository      │       │ Repository      │
        └────────┬────────┘       └────────┬────────┘
                 │                         │
                 ▼                         ▼
             HC-05                  Room / DataStore
                 │
                 ▼
             Arduino
```

This would make the application easier to test, maintain, and extend.

---

# 📜 License

This project is provided for educational and IoT development purposes.

Add an appropriate open-source license before publishing if you intend other developers to reuse or distribute the project.

For example:

```text
MIT License
```

---

# 👨‍💻 Project

**SWITCH PRO** is a simple Android-to-Arduino IoT control system demonstrating:

* Android Bluetooth communication
* HC-05 Bluetooth SPP
* Arduino integration
* Local data persistence
* Android AlarmManager
* BroadcastReceiver
* Scheduled IoT automation
* Temperature-based control concepts

---

## ⭐ If You Find This Project Useful

If this project helps you learn about Android Bluetooth communication and IoT automation, consider giving the repository a ⭐ on GitHub.

```text
Android App
     │
     │ Bluetooth SPP
     ▼
   HC-05
     │
     │ UART
     ▼
  Arduino
     │
     ├── ON
     ├── OFF
     ├── AUTO
     └── Temperature-based control
```
