# Memorize It

Memorize It includes both Android and browser versions of a personalized brain training puzzle app designed around familiar personal memories.
Users can add personal photos, likes, and interests, and the app turns these into a memory matching game.

## Features

- Profile setup with name, likes, and interests
- Photo upload from device gallery
- Local profile persistence using SharedPreferences + Kotlin serialization
- Personalized memory match puzzle using uploaded photos and text cues

## Platforms

- Android app: Kotlin + Jetpack Compose in `app/`
- Web app: HTML/CSS/JavaScript in `web/`

## Project Structure

- app/src/main/java/com/memorizeit/MainActivity.kt: UI flow and game interactions
- app/src/main/java/com/memorizeit/data/ProfileStore.kt: local profile storage
- app/src/main/java/com/memorizeit/game/GameFactory.kt: builds puzzle cards from personal data
- app/src/main/java/com/memorizeit/model/Models.kt: app models

## Build and Run

### Android

1. Open this folder in Android Studio.
2. Let Gradle sync and install any requested Android SDK components.
3. Run the app on an emulator or Android device.

### Web

1. Open `web/index.html` in a browser, or run a local static server.
2. Optional local server from workspace root: `python -m http.server 8080`
3. Open `http://localhost:8080/web/`

## Notes for Dementia-Friendly Iteration

The current version is a functional baseline. For production use with dementia patients, consider:

- Larger fonts and higher contrast options
- Optional audio prompts and voice instructions
- Caregiver mode for content setup
- Session analytics for progress tracking
- Calmer pacing and reduced time pressure
