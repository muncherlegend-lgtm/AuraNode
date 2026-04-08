# AuraNode

AuraNode is an offline Android quiz expedition about Altai Krai and Barnaul, built for a mobile app case competition.

The project keeps the required contest flow:

- `Menu -> Quiz -> Result`
- 3 difficulty levels: `Cadet`, `Engineer`, `Cosmonaut`
- 4 answer options by default in `Classic` mode
- visual answer feedback
- local data and offline-first work

At the same time, the app goes beyond the baseline with a showcase product layer:

- `Altai Atlas`: unlockable offline map hub with regional nodes and route connections
- `Challenge Layer`: `Classic`, `Sprint`, and `Legend` modes
- `Showcase Results`: medals, achievements, hall of fame, and share-card export
- dynamic themes and atmospheric backgrounds
- persisted settings and progress via `DataStore`
- jury-safe preset and demo reset flow for predictable competition presentation
- `Import Material -> Generate Quiz Pack -> Play`
- AI BYOK flow with offline fallback draft generation

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel + StateFlow
- Dagger Hilt
- Preferences DataStore
- Android Keystore-backed local encryption for AI keys
- file import via `OpenDocument`
- PDF / DOCX / TXT / Markdown text extraction

## Architecture

- `MVVM`: all UI is driven by `QuizUiState` from `QuizViewModel`
- `Repository`: a single source of truth for questions, settings, atlas data, themes, achievements, and progress
- `Data sources`:
  - local config assets in `app/src/main/assets/config`
  - persistent state in `DataStore`
- `Use cases`:
  - scoring
  - atlas unlock logic
  - achievement evaluation
  - legend eligibility

## Key Features

### 1. Classic contest-safe flow

- 3 difficulty levels
- question card with 4 answer options
- progress and optional timer
- result screen with retry
- `Jury mode` preset keeps the default route strict and presentation-safe
- `Demo/Jury reset` clears progress before a live presentation

### 2. Altai Atlas

- offline map of regional landmarks
- unlock nodes after correct answers
- route visualization and node details

### 3. Challenge Layer

- `Classic`: baseline judging mode
- `Sprint`: faster pacing with streak and time bonus
- `Legend`: unlocked boss-style mode for strong runs

### 4. Showcase Results

- medals and accuracy metrics
- achievements
- hall of fame
- result share card via Android share sheet

### 5. Config-driven content

Project content can be tuned without rewriting UI code:

- `app/src/main/assets/config/questions.json`
- `app/src/main/assets/config/quiz_settings.json`
- `app/src/main/assets/config/themes.json`
- `app/src/main/assets/config/atlas.json`
- `app/src/main/assets/config/achievements.json`

### 6. Custom quiz packs from files

- import supported formats:
  - `txt`
  - `md`
  - `pdf`
  - `docx`
- generate a new quiz pack with 3 difficulty levels
- keep the official Altai pack intact for the competition route
- save imported packs locally in app-private storage
- replay imported packs from `My Packs`

### 7. AI generation strategy

- `BYOK`: the user enters their own API key
- provider presets:
  - `Gemini API`
  - `OpenRouter`
- no shared secret is baked into the APK
- if cloud generation is unavailable, AuraNode falls back to a local offline draft generator
- official Altai gameplay remains fully offline even without AI setup

## Validation and Stability

- invalid theme colors are filtered out with safe fallbacks
- broken or missing config content falls back to bundled defaults
- core domain rules are covered by unit tests:
  - scoring
  - achievements
  - atlas unlocks
  - legend mode eligibility
- config parser and validation are covered by unit tests for malformed questions, themes, settings, and atlas nodes
- compose instrumentation smoke tests are included for navigation, theme selection, atlas opening, retry, and jury reset controls

## Build

From the project root:

```powershell
.\gradlew.bat :app:assembleDebug
```

Release build:

```powershell
.\gradlew.bat :app:assembleRelease
```

Unit tests:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Android test compilation:

```powershell
.\gradlew.bat :app:compileDebugAndroidTestKotlin
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK output:

```text
app/build/outputs/apk/release/app-release.apk
```

Contest deliverables directory:

```text
deliverables/
```

## Review Path

If you want to judge the project quickly:

1. Launch the app and keep the default `Altai` route.
2. Try `Cadet` in `Classic` mode for the strict contest path.
3. Return to menu and open `Import Material`.
4. Load a `txt`, `md`, `pdf`, or `docx` file.
5. Generate a custom quiz pack with AI or offline fallback.
6. Open `My Packs` and replay the generated set.

## Project Highlights For Review

- visually polished and fully offline
- stronger-than-required architecture
- clear separation of UI, state, and domain rules
- configurable content and themes
- extended product depth without breaking the required case path
- custom file-to-quiz pipeline without a backend
- optional AI layer that does not block offline use
