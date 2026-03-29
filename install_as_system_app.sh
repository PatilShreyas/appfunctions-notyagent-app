#!/bin/bash

# 1. Define the default macOS paths
ADB_PATH="$HOME/Library/Android/sdk/platform-tools/adb"
EMULATOR_PATH="$HOME/Library/Android/sdk/emulator/emulator"
APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"

# --- STEP 1: BUILD THE APK ---
echo "🔨 Building the Agent APK via Gradle..."
chmod +x ./gradlew
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ ERROR: Gradle build failed. Please check your code in Android Studio."
    exit 1
fi

if [ ! -f "$APK_PATH" ]; then
    echo "❌ ERROR: APK not found at $APK_PATH."
    exit 1
fi
echo "✅ Build successful! APK found."

# --- STEP 2: CHOOSE THE EMULATOR ---
echo "📱 Fetching available emulators..."
options=($($EMULATOR_PATH -list-avds))

if [ ${#options[@]} -eq 0 ]; then
    echo "❌ ERROR: No emulators found."
    exit 1
fi

if [ ${#options[@]} -eq 1 ]; then
    AVD_NAME="${options[0]}"
    echo "✅ Only one emulator found: $AVD_NAME"
else
    echo "------------------------------------------------"
    echo "Multiple emulators detected. Which one should we use?"
    echo "------------------------------------------------"
    PS3="Please enter the number for your choice: "
    select opt in "${options[@]}" "Quit"; do
        if [[ "$opt" == "Quit" ]]; then
            exit 0
        elif [[ -n "$opt" ]]; then
            AVD_NAME=$opt
            echo "🚀 Selection confirmed: $AVD_NAME"
            break
        else
            echo "❌ Invalid selection. Pick a number from the list."
        fi
    done
fi

# --- STEP 3: CLEAN SLATE ---
echo "🧹 Shutting down running emulators..."
$ADB_PATH kill-server > /dev/null 2>&1
killall qemu-system-aarch64 2>/dev/null
killall qemu-system-x86_64 2>/dev/null
sleep 3

# --- STEP 4: BOOT ---
echo "⏳ Booting emulator with writable-system (this takes a minute)..."
$EMULATOR_PATH -avd "$AVD_NAME" -writable-system -no-snapshot-load > /dev/null 2>&1 &

$ADB_PATH wait-for-device
while [ "$($ADB_PATH shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 2; done
echo "✅ Emulator Booted!"

# --- STEP 4.5: NORMAL APK INSTALL (THE FIX) ---
echo "📲 Performing normal install first to register package data..."
# -t allows test packages, -r replaces existing, -d allows downgrades
$ADB_PATH install -t -r -d "$APK_PATH"
echo "✅ Normal install complete! OS has registered the app."

# --- STEP 5: DISABLE VERIFIED BOOT ---
echo "🔓 Disabling Verified Boot..."
$ADB_PATH root
sleep 2
$ADB_PATH disable-verity
sleep 2
$ADB_PATH reboot

# --- STEP 6: WAIT FOR REBOOT ---
echo "⏳ Waiting for reboot to finish..."
$ADB_PATH wait-for-device
while [ "$($ADB_PATH shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 2; done
echo "✅ Reboot Completed!"

# --- STEP 7: REMOUNT & PUSH ---
echo "📂 Remounting system as Read/Write..."
$ADB_PATH root
sleep 2
$ADB_PATH remount
sleep 2

echo "📦 Pushing Agent APK to /system/priv-app/ to grant Privileged status..."
$ADB_PATH shell mkdir -p /system/priv-app/NotyAgentApp
$ADB_PATH push "$APK_PATH" /system/priv-app/NotyAgentApp/NotyAgentApp.apk

# --- STEP 8: FINAL REBOOT ---
echo "🔄 Executing final reboot to finalize system app registration..."
$ADB_PATH reboot

$ADB_PATH wait-for-device
while [ "$($ADB_PATH shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 2; done

echo "🎉 DONE! Your Agent is now a fully functional System App."