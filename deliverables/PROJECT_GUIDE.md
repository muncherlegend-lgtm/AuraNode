# AuraNode Project Guide

## 1. Что это за проект

`AuraNode` — это мобильная викторина про Алтайский край и Барнаул, сделанная на `Android Native`, `Kotlin` и `Jetpack Compose`.

Базовый конкурсный путь у проекта простой и прозрачный:

`Главная -> Викторина -> Результат`

При этом проект идёт дальше минимального кейса и добавляет:

- отдельный экран карты региона;
- отдельный экран настроек;
- отдельный экран материалов;
- локальный конструктор пользовательских наборов из документов;
- локальное сохранение прогресса и результатов;
- шаринг результата в виде карточки и PNG.

## 2. Главные продуктовые принципы

Проект построен вокруг пяти принципов:

1. Основной путь должен быть коротким.
   Пользователь сразу видит `Начать`, выбирает `режим` и `уровень`, после чего попадает в викторину без лишних экранов.

2. Игровая механика должна быть понятной.
   В боевом сценарии всегда используется вопрос и `4 варианта ответа`, без свободного текстового ввода.

3. Продукт должен работать локально.
   Официальный маршрут, настройки, прогресс, пользовательские наборы и импорт работают без зависимости от облака.

4. Пользовательские материалы должны встраиваться в ту же систему.
   Импортированный материал после сохранения превращается в обычный `QuizPack`, а не в отдельный особый режим.

5. Визуальный язык должен быть спокойным и взрослым.
   Меньше “игрового шума”, больше понятных экранов, чистой типографики и аккуратной иерархии.

## 3. Как устроен проект по слоям

### UI

UI находится в:

- `app/src/main/java/com/example/newapp/ui/`
- `app/src/main/java/com/example/newapp/navigation/`

Главные точки:

- [MenuScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/menu/MenuScreen.kt)
- [QuizScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/quiz/QuizScreen.kt)
- [ResultScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/result/ResultScreen.kt)
- [AtlasScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/atlas/AtlasScreen.kt)
- [SettingsScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/settings/SettingsScreen.kt)
- [ThemesScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/themes/ThemesScreen.kt)
- [MaterialsScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/materials/MaterialsScreen.kt)

Навигация собрана в:

- [AuraNodeDestination.kt](/Y:/projects/app/src/main/java/com/example/newapp/navigation/AuraNodeDestination.kt)
- [AuraNodeNavHost.kt](/Y:/projects/app/src/main/java/com/example/newapp/navigation/AuraNodeNavHost.kt)

### State и orchestration

Центральное состояние интерфейса живёт в:

- [QuizUiState.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/quiz/QuizUiState.kt)

Главный координатор сценариев:

- [QuizViewModel.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/quiz/QuizViewModel.kt)

`QuizViewModel` отвечает за три большие ветки:

- запуск официальной викторины;
- работу с пользовательскими материалами и draft-конструктором;
- карту, результат, прогресс и шаринг.

### Data layer

Центральный интерфейс репозитория:

- [QuizRepository.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/repository/QuizRepository.kt)

Основная реализация:

- [QuizRepositoryImpl.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/repository/QuizRepositoryImpl.kt)

Репозиторий объединяет:

- локальные конфиги из `assets`;
- `DataStore` для настроек и прогресса;
- хранилище пользовательских наборов;
- импорт документов;
- use case-слой генерации draft и расчёта результата.

### Domain/use cases

Логика вынесена в отдельные use case’ы:

- [RunScoringUseCase.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/RunScoringUseCase.kt)
- [AtlasUnlockUseCase.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/AtlasUnlockUseCase.kt)
- [AchievementEvaluatorUseCase.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/AchievementEvaluatorUseCase.kt)
- [LegendEligibilityUseCase.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/LegendEligibilityUseCase.kt)
- [ImportedDraftFactory.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/ImportedDraftFactory.kt)
- [OfflineQuizPackGenerator.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/OfflineQuizPackGenerator.kt)

Это делает проект сильнее по архитектуре: UI не содержит бизнес-логику, а ViewModel не превращается в склад алгоритмов.

## 4. Как работает основной сценарий

### Главная

На [MenuScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/menu/MenuScreen.kt) пользователь:

- выбирает уровень сложности;
- выбирает режим;
- запускает официальный маршрут;
- или открывает вторичные разделы: карту, материалы, настройки и темы.

### Викторина

На [QuizScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/quiz/QuizScreen.kt):

- отображается вопрос;
- показываются ровно 4 варианта ответа;
- после выбора подсвечивается правильный или неправильный ответ;
- дальше идёт переход к следующему вопросу;
- при необходимости работает таймер.

Внутри это управляется из `QuizViewModel` через:

- загрузку вопросов из выбранного `QuizPack`;
- расчёт очков;
- обновление серии;
- накопление прогресса прохождения.

### Результат

На [ResultScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/result/ResultScreen.kt):

- отображается итоговый счёт;
- показывается точность;
- различаются официальный маршрут и пользовательский набор;
- можно повторить прохождение;
- можно открыть карту;
- можно поделиться результатом.

## 5. Что такое импорт и зачем он нужен

Импорт — это локальный конструктор учебных наборов из пользовательских документов.

Он позволяет взять свой материал и превратить его в набор вопросов, который запускается тем же quiz-движком, что и официальный маршрут.

### Поддерживаемые форматы

Фактически поддерживаются:

- `TXT`
- `MD`
- `PDF`
- `DOCX`

Проверочные файлы лежат в:

- [import-samples](/Y:/projects/deliverables/import-samples)

### Где находится логика импорта

Ключевые точки:

- [DocumentImportDataSource.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/source/DocumentImportDataSource.kt)
- [DocumentTextExtraction.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/source/DocumentTextExtraction.kt)
- [ImportedDraftFactory.kt](/Y:/projects/app/src/main/java/com/example/newapp/domain/usecase/ImportedDraftFactory.kt)
- [MaterialsScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/materials/MaterialsScreen.kt)

### Что происходит по шагам

#### Шаг 1. Выбор файла

Пользователь выбирает файл на экране `Материалы`.

#### Шаг 2. Извлечение текста

`DocumentImportDataSource`:

- определяет имя и расширение файла;
- проверяет, что формат поддерживается;
- достаёт текст из `TXT`, `MD`, `PDF` или `DOCX`;
- очищает текст;
- делает короткий preview;
- считает приблизительное число вопросов.

На этом этапе создаётся `ImportedDocument`.

#### Шаг 3. Построение draft

`ImportedDraftFactory`:

- делит текст на логические разделы;
- пытается распознать заголовки и границы секций;
- создаёт начальный список `QuestionDraft`;
- заполняет черновые вопросы на основе локальной offline-генерации.

На этом этапе создаётся `ImportedDocumentDraft`.

#### Шаг 4. Ручная доработка

На экране `Материалы` пользователь может:

- редактировать название набора;
- редактировать описание;
- включать и выключать разделы;
- пересобирать вопросы только по выбранным разделам;
- вручную править текст вопроса;
- вручную править 4 варианта ответа;
- менять правильный ответ;
- менять сложность;
- редактировать пояснение;
- удалять вопросы;
- добавлять вопросы вручную.

#### Шаг 5. Сохранение

После сохранения draft превращается в обычный `QuizPack`.

Это ключевой принцип проекта:

импорт не создаёт отдельную “особую сущность”, а встраивается в ту же модель данных, что и официальный контент.

За счёт этого пользовательский набор:

- появляется в списке материалов;
- запускается штатной логикой викторины;
- работает через те же 4 варианта ответа;
- не ломает карту официального маршрута.

## 6. Что хранится локально и где

### Конфиги приложения

Оффлайн-данные лежат в:

- [assets/config](/Y:/projects/app/src/main/assets/config)

Там находятся:

- официальный набор вопросов;
- карта-атлас;
- темы;
- стартовые настройки.

### Настройки и прогресс

Пользовательские настройки и прогресс хранятся через `DataStore`.

Ключевой источник:

- [QuizPreferencesDataSource.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/source/QuizPreferencesDataSource.kt)

Там сохраняются:

- тема;
- таймер;
- количество вопросов;
- перемешивание;
- анимации;
- звук;
- тактильный отклик;
- прогресс карты;
- лучшие результаты.

### Пользовательские наборы

Сохранённые кастомные наборы лежат отдельно и подгружаются через:

- [QuizPackStorageDataSource.kt](/Y:/projects/app/src/main/java/com/example/newapp/data/source/QuizPackStorageDataSource.kt)

## 7. Как работает карта

Карта — это отдельный экран, а не overlay поверх всего приложения.

Главные точки:

- [AtlasScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/atlas/AtlasScreen.kt)
- [AtlasMapCard.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/components/AtlasMapCard.kt)
- [AtlasPanelMode.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/atlas/AtlasPanelMode.kt)

Принцип такой:

- сначала пользователь видит карту;
- детали узла открываются только по запросу;
- панель может быть скрыта;
- карта не должна быть постоянно перекрыта тяжёлым блоком.

Разблокировки карты завязаны на официальный маршрут и не зависят от пользовательских наборов.

## 8. Как работает шаринг результата

Главные файлы:

- [ResultScreen.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/screens/result/ResultScreen.kt)
- [ResultShareCard.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/components/ResultShareCard.kt)
- [ResultShareManager.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/share/ResultShareManager.kt)
- [ResultShareContent.kt](/Y:/projects/app/src/main/java/com/example/newapp/ui/share/ResultShareContent.kt)

Логика такая:

1. Из результата строится единая модель шаринга.
2. Из неё генерируются:
   текст для копирования;
   preview-карточка;
   PNG для сохранения и отправки.
3. Пользователь может:
   открыть системное меню отправки;
   сохранить PNG;
   скопировать текст.

Это сделано единым слоем, чтобы preview, текст и итоговое изображение не расходились по смыслу.

## 9. Почему проект сильнее базового кейса

Если смотреть строго по конкурсным критериям, проект усиливает каждую категорию:

- `Архитектура`
  MVVM, repository, use case’ы, разделение UI и бизнес-логики.

- `Чистота кода`
  отдельные экраны, отдельные data source, отдельные use case’ы, централизованное состояние.

- `Стабильность`
  локальная работа без обязательного облака, проверка форматов, ограничения на некорректный импорт, сохранение состояния.

- `Сложность проекта`
  карта, прогресс, темы, шаринг, локальный импорт-конструктор и хранение пользовательских наборов.

- `Внешний вид`
  Compose UI, отдельные спокойные экраны, тематические пресеты, улучшенная карточка шаринга.

## 10. Что ещё можно сделать, чтобы проект выглядел на 101/101

Сейчас проект уже очень сильный, но если добивать его до ощущения “безоговорочного максимума”, то лучший следующий шаг такой:

1. Удалить остатки legacy-классов, которые уже не участвуют в продукте.
2. Разбить самые крупные файлы `ViewModel` и `ResultScreen` на 2-3 smaller feature-файла.
3. Добавить 2-3 UI smoke-теста именно под новый share-flow и импорт материалов.
4. Добавить 4-6 скриншотов в основной `README`.
5. Обновить GitHub Release после каждого заметного polish-прохода, чтобы APK и репозиторий всегда совпадали.

## 11. Что показывать жюри за 2 минуты

Лучший короткий сценарий демонстрации:

1. Открыть `Главную`.
2. Показать выбор уровня и запуск официального маршрута.
3. Ответить на пару вопросов.
4. Показать экран результата и диалог шаринга.
5. Открыть карту из результата.
6. Открыть `Материалы`, импортировать один sample-файл и показать draft-конструктор.

Именно этот путь лучше всего демонстрирует, что проект не только выполняет базовый кейс, но и качественно расширяет его.
