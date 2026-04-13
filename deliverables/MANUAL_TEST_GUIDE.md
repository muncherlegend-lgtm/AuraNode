# AuraNode Manual Test Guide

## 1. Files And Paths

- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Official config files:
  - `app/src/main/assets/config/questions.json`
  - `app/src/main/assets/config/quiz_settings.json`
  - `app/src/main/assets/config/themes.json`
  - `app/src/main/assets/config/atlas.json`
  - `app/src/main/assets/config/achievements.json`
- Import samples:
  - `deliverables/import-samples/structured_notes.txt`
  - `deliverables/import-samples/history_article.md`
  - `deliverables/import-samples/travel_digest.pdf`
  - `deliverables/import-samples/museum_overview.docx`
  - `deliverables/import-samples/messy_reference.txt`

## 2. Preparing The Phone

1. Copy `app-release.apk` to the phone.
2. Install the APK.
3. Copy the whole `deliverables/import-samples/` folder to the phone storage so it is visible in the system file picker.
4. Launch the app from a clean install.

## 3. Main Contest Flow

### Menu

Check:

- the app opens on `Главная`
- the `Начать` button is visible without scrolling
- 3 difficulty chips are visible: `Кадет`, `Инженер`, `Космонавт`
- mode chips are visible and readable
- secondary sections `Карта`, `Материалы`, `Настройки` are below the main block

### Quiz

Steps:

1. On `Главная`, leave the official route selected.
2. Choose `Кадет`.
3. Tap `Начать`.

Check:

- a question is shown
- there are exactly 4 answer options
- tapping an option locks the question
- correct / incorrect state is highlighted visually
- feedback text appears
- the app moves to the next question
- timer is visible when enabled in settings

### Result

Finish the quiz and check:

- `Результат` screen opens automatically
- score and accuracy are visible
- `Пройти ещё раз` works
- `В главное меню` works
- for the official route, `Открыть карту` is visible after progress unlocks nodes

## 4. Settings Screen

Steps:

1. Open `Настройки`.
2. Change several values.

Check:

- timer toggle works
- seconds per question can be increased and decreased
- question count can be increased and decreased
- shuffle questions toggle works
- shuffle options toggle works
- compact UI toggle works
- animation toggle works
- haptics toggle works
- sound toggle works
- theme selection changes the app look
- after closing and reopening the app, settings are preserved

## 5. Atlas Screen

Steps:

1. Open `Карта` from `Главная`.
2. Pan and zoom the map.
3. Tap a node.
4. Close the node details.

Check:

- the map is visible immediately
- no heavy panel blocks the map by default
- node tap opens details
- closing details returns to a clean map view
- `К последней точке` works after completing at least one official run

## 6. Materials Screen And Import Builder

### Import Support

Run the same flow for:

- `structured_notes.txt`
- `history_article.md`
- `travel_digest.pdf`
- `museum_overview.docx`
- `messy_reference.txt`

Steps:

1. Open `Материалы`.
2. Tap `Импортировать файл`.
3. Select one sample file.

Check:

- the file is read without crash
- a draft appears
- sections are visible
- question drafts are created

### Draft Editing

Check:

- title can be changed
- description can be changed
- sections can be enabled and disabled
- `Обновить вопросы` rebuilds draft questions
- question text can be edited
- all 4 options can be edited
- correct answer can be changed
- difficulty can be changed
- explanation can be edited
- question can be deleted
- new question can be added manually
- `Сохранить набор` works

### Launching Custom Pack

After saving:

1. Stay on `Материалы`.
2. Find the saved pack in the list.
3. Pick a difficulty.
4. Launch the pack.

Check:

- custom pack opens in quiz flow
- it still uses 4 answer options
- result screen clearly shows it is a user material, not the official route
- custom pack progress does not affect atlas unlocks

## 7. Result Sharing

Finish any run and check:

1. Tap `Поделиться`.
2. Verify the preview dialog opens.
3. Tap `Поделиться`.
4. Reopen and tap `Сохранить PNG`.
5. Reopen and tap `Копировать текст`.

Check:

- preview card opens without crash
- system share sheet opens
- PNG save completes without crash
- copied text can be pasted into notes or messenger

## 8. Data Reset

On `Настройки`:

1. Tap `Очистить материалы`.
2. Tap `Сбросить результаты`.

Check:

- custom packs disappear from `Материалы`
- official route still works
- atlas and result history are reset

## 9. Regression Checklist

Verify:

- default route is still the official Altai pack
- official questions remain readable and in Russian
- no AI / jury / demo language appears in the active UI
- the main path always uses 4 options
- app works offline for the official route

## 10. What To Show The Jury Quickly

If time is short, demo this sequence:

1. `Главная` with 3 difficulty levels.
2. One answered question with visual feedback.
3. `Результат` with share preview.
4. `Карта` with a node opened.
5. `Материалы` importing one sample file and opening a draft.
