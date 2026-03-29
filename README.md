# Android AppFunctions Sample Agent for NotyKT 🤖

NotyAgent is an experimental Android application demonstrating the power of **Android AppFunctions** (introduced in Android 16 / API 35+).

This project acts as an **Agent**, capable of executing predefined functions (like reading, creating, and deleting notes) exposed by a target **Tool** app seamlessly in the background, without requiring deep links, explicit Intents, or UI navigation.

> Note: It is not a real-agent app but just mocks the behavior of the agent app.

## 🔗 The Target "Tool" App: NotyKT
For this Agent to work, it needs a Tool app that exposes AppFunctions. This project pairs with **[NotyKT](https://github.com/PatilShreyas/NotyKT)**.

The AppFunctions integration for NotyKT was done in this Pull Request:
👉 **[NotyKT PR #823: AppFunctions Integration](https://github.com/PatilShreyas/NotyKT/pull/823)**

---

## 🏗️ How it Works

Android AppFunctions provide a structured, type-safe way for apps to expose capabilities to the system and privileged Agent apps.

1. **App (NotyKT)**: Uses the `androidx.appfunctions` compiler to generate a metadata schema of its available actions (`listNotes`, `createNote`, etc.) and registers an `AppFunctionService`.
2. **Agent (NotyAgent)**: Constructs an `ExecuteAppFunctionRequest` with the target package name and function ID, passing along typed parameters (like note title and content).
3. **The OS**: The Android System securely routes the request from the Agent to the Tool, executes the function in the Tool's background service context, and returns the serialized result back to the Agent.

*Note: In the current Android Developer Previews, Agent apps must be installed as **Privileged System Apps** to execute AppFunctions.*

---

## 🛠️ Prerequisites

Before you begin, ensure your environment is set up correctly. **This is critical for the installation script to succeed.**

1. **Android Studio**: Ladybug (or newer) with Android 16 SDK support.
2. **Emulator Requirements**:
    * Must be an **Android 16 (VanillaIceCream or Baklava)** image.
    * Must be a **"Google APIs"** image (NOT "Google Play" — Play images cannot be rooted/remounted).

---

## 🚀 Step-by-Step Installation Guide

### Step 1: Install the Tool App (NotyKT)
1. Clone the NotyKT repository.
2. Install the NotyKT app (any one can work - simpleapp, composeapp) and also run the backend server locally.
3. Build and deploy the app to your Android 16 emulator.
4. Open NotyKT at least once and add a sample note so the database is initialized.

### Step 2: Prepare the Agent App
1. Clone this repository (`appfunctions-notyagent-app`).
2. Open the project in Android Studio and let Gradle sync.

### Step 3: Run the Installation Script
Because Agent apps must be privileged system apps, you cannot simply click "Run" in Android Studio. We have provided a shell script that automates the complex process of unlocking the emulator and installing the app to `/system/priv-app/`.

1. Open your terminal in the root of this project.
2. Make the script executable:
   ```bash
   chmod +x install_as_system_app.sh
   ```
   
3. Run the script
   ```bash
   ./install_as_system_app.sh
   ```

### Step 4: Test the Integration

1. Once the emulator boots up, you will find NotyAgent in your app drawer. 
2. Open the app and use the UI to fetch notes, add new ones, or delete them. 
3. The Agent will communicate directly with NotyKT via the AppFunctions system service!

#### Test prompts

- `show me my notes`: (read) Lists the notes currently available in NotyKT app
- `add a note about info of appfunctions`: (create) Adds a new note in the NotyKT about the info of AppFunctions.
- `make it short`: (update) Shorts the previously added note

## 🛑 Troubleshooting

**1. Code 1003: Function Not Found" or App returns no data**

Cause: The OS hasn't indexed the Tool app's functions yet.

Fix: Ensure you have opened the NotyKT app at least once. The script automatically enables the enable_app_functions_schema_parser flag, but sometimes a device reboot helps the OS re-index the package.
