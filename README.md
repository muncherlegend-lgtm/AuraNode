# Маршрут Алтая

«Маршрут Алтая» — это Android-викторина об Алтайском крае и Барнауле, сделанная на Kotlin и Jetpack Compose для мобильного конкурса.

## What The App Does

- keeps the main contest flow: `Главная -> Викторина -> Результат`
- supports 3 difficulty levels: `Кадет`, `Инженер`, `Космонавт`
- uses 4 answer options in the main gameplay path
- stores settings and progress locally
- works fully offline for the official route

## Extra Product Depth

- separate `Карта`, `Материалы`, and `Настройки` screens
- calm atlas screen with unlockable regional nodes
- local material import from `TXT`, `MD`, `PDF`, `DOCX`
- editable draft builder for custom quiz packs
- result sharing with preview, PNG save, and text copy
- adult theme presets and localized Russian UI

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel + StateFlow
- Dagger Hilt
- Preferences DataStore
- PDF / DOCX / TXT / Markdown text extraction

## Architecture

- `MVVM`: UI is driven by `QuizUiState` from `QuizViewModel`
- `Repository`: central access point for quiz data, settings, materials, progress, atlas, and themes
- `Data sources`:
  - local config assets in `app/src/main/assets/config`
  - persistent state in `DataStore`
  - local custom pack storage
- `Use cases`:
  - scoring
  - atlas unlock logic
  - achievement evaluation
  - legend eligibility
  - imported draft generation

## Important Paths

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Config assets: `app/src/main/assets/config/`
- Import samples: `deliverables/import-samples/`
- Project guide: `deliverables/PROJECT_GUIDE.md`
- Manual test guide: `deliverables/MANUAL_TEST_GUIDE.md`

## Build

From the project root:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:lintDebug
```

## Review Shortcut

1. Open the app and stay on the default official route.
2. Start `Кадет` in `Основной` mode.
3. Finish one run and open the result screen.
4. Open the atlas from the result screen.
5. Open `Материалы`, import one sample file, edit the draft, save the custom pack, and launch it.

## Deliverables

- public GitHub repository
- built APK
- optional: sample import files and manual test guide from `deliverables/`
