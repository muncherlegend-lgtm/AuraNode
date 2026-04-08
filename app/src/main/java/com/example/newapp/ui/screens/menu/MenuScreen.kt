package com.example.newapp.ui.screens.menu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newapp.R
import com.example.newapp.data.model.AiProvider
import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.HomeContentPreference
import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode
import com.example.newapp.data.model.QuizPackType
import com.example.newapp.ui.AuraNodeTestTags
import com.example.newapp.ui.components.AuraFactChip
import com.example.newapp.ui.components.AuraNodeSurfaceCard
import com.example.newapp.ui.components.DifficultyRouteCard
import com.example.newapp.ui.components.HallOfFameRunCard
import com.example.newapp.ui.components.QuizModeCard
import com.example.newapp.ui.components.QuizPackCard
import com.example.newapp.ui.components.ThemePresetCard
import com.example.newapp.ui.quiz.QuizUiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    uiState: QuizUiState,
    onDifficultySelected: (Difficulty) -> Unit,
    onModeSelected: (QuizMode) -> Unit,
    onPackSelected: (String) -> Unit,
    onDeletePack: (String) -> Unit,
    onImportDocumentSelected: (Uri) -> Unit,
    onGenerateCloudPack: () -> Unit,
    onGenerateOfflinePack: () -> Unit,
    onDismissImportPreview: () -> Unit,
    onConfirmAiConsent: () -> Unit,
    onDismissAiConsent: () -> Unit,
    onDismissGenerationMessage: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onTimerToggle: (Boolean) -> Unit,
    onCompactUiToggle: (Boolean) -> Unit,
    onShuffleToggle: (Boolean) -> Unit,
    onMotionToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onJuryModeToggle: (Boolean) -> Unit,
    onDemoResetOnLaunchToggle: (Boolean) -> Unit,
    onResetDemoProgress: () -> Unit,
    onAnswerModeSelected: (AnswerMode) -> Unit,
    onHomePreferenceSelected: (HomeContentPreference) -> Unit,
    onAiProviderSelected: (AiProvider) -> Unit,
    onCloudGenerationEnabled: (Boolean) -> Unit,
    onAiApiKeyChanged: (String) -> Unit,
    onGeminiModelChanged: (String) -> Unit,
    onOpenRouterModelChanged: (String) -> Unit,
    onCompleteOnboarding: () -> Unit,
    onOpenAtlas: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        LoadingMenuState(modifier = modifier)
        return
    }

    val settings = uiState.quizSettings
    val compact = settings.compactUi
    var showSettingsSheet by remember { mutableStateOf(false) }
    val orderedPacks = remember(uiState.quizPacks, settings.homeContentPreference) {
        when (settings.homeContentPreference) {
            HomeContentPreference.OFFICIAL_FIRST -> uiState.quizPacks.sortedBy { it.type != QuizPackType.OFFICIAL_ALTAI }
            HomeContentPreference.CUSTOM_FIRST -> uiState.quizPacks.sortedBy { it.type == QuizPackType.OFFICIAL_ALTAI }
            HomeContentPreference.BALANCED -> uiState.quizPacks
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) onImportDocumentSelected(uri) }
    )

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false }
        ) {
            SettingsSheetContent(
                uiState = uiState,
                onTimerToggle = onTimerToggle,
                onCompactUiToggle = onCompactUiToggle,
                onShuffleToggle = onShuffleToggle,
                onMotionToggle = onMotionToggle,
                onHapticsToggle = onHapticsToggle,
                onSoundToggle = onSoundToggle,
                onJuryModeToggle = onJuryModeToggle,
                onDemoResetOnLaunchToggle = onDemoResetOnLaunchToggle,
                onResetDemoProgress = onResetDemoProgress,
                onAnswerModeSelected = onAnswerModeSelected,
                onHomePreferenceSelected = onHomePreferenceSelected,
                onAiProviderSelected = onAiProviderSelected,
                onCloudGenerationEnabled = onCloudGenerationEnabled,
                onAiApiKeyChanged = onAiApiKeyChanged,
                onGeminiModelChanged = onGeminiModelChanged,
                onOpenRouterModelChanged = onOpenRouterModelChanged
            )
        }
    }

    uiState.importedDocumentPreview?.let { importedDocument ->
        ModalBottomSheet(onDismissRequest = onDismissImportPreview) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.import_preview_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AuraFactChip(
                    text = importedDocument.displayName,
                    accent = MaterialTheme.colorScheme.primary,
                    compact = true
                )
                Text(
                    text = stringResource(
                        R.string.import_preview_meta,
                        importedDocument.sourceExtension.uppercase(),
                        importedDocument.estimatedQuestionCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = importedDocument.previewExcerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onGenerateCloudPack,
                    enabled = !uiState.isGeneratingPack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuraNodeTestTags.MENU_GENERATE_CLOUD)
                ) {
                    Text(text = stringResource(R.string.import_generate_cloud))
                }
                OutlinedButton(
                    onClick = onGenerateOfflinePack,
                    enabled = !uiState.isGeneratingPack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuraNodeTestTags.MENU_GENERATE_OFFLINE)
                ) {
                    Text(text = stringResource(R.string.import_generate_offline))
                }
            }
        }
    }

    if (uiState.isAiConsentSheetVisible) {
        AlertDialog(
            onDismissRequest = onDismissAiConsent,
            title = { Text(text = stringResource(R.string.ai_consent_title)) },
            text = { Text(text = stringResource(R.string.ai_consent_body)) },
            confirmButton = {
                Button(onClick = onConfirmAiConsent) {
                    Text(text = stringResource(R.string.ai_consent_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissAiConsent) {
                    Text(text = stringResource(R.string.ai_consent_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.MENU_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
        ) {
            item {
                AuraNodeSurfaceCard(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = if (compact) 18.dp else 22.dp,
                            vertical = if (compact) 18.dp else 22.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AuraFactChip(
                            text = if (uiState.isOfficialPackSelected) {
                                stringResource(R.string.menu_location_label)
                            } else {
                                stringResource(R.string.menu_custom_mode_label)
                            },
                            compact = true
                        )
                        if (settings.juryModeEnabled) {
                            AuraFactChip(
                                text = stringResource(R.string.menu_jury_mode_enabled),
                                accent = MaterialTheme.colorScheme.secondary,
                                compact = true
                            )
                        }
                        Text(
                            text = stringResource(R.string.menu_title),
                            style = if (compact) {
                                MaterialTheme.typography.displaySmall
                            } else {
                                MaterialTheme.typography.displayLarge
                            },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isOfficialPackSelected) {
                                stringResource(R.string.menu_subtitle)
                            } else {
                                stringResource(R.string.menu_custom_subtitle)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenAtlas,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(AuraNodeTestTags.MENU_OPEN_ATLAS)
                            ) {
                                Text(text = stringResource(R.string.menu_open_atlas))
                            }
                            Button(
                                onClick = {
                                    importLauncher.launch(
                                        arrayOf(
                                            "text/plain",
                                            "text/markdown",
                                            "application/pdf",
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(AuraNodeTestTags.MENU_IMPORT_MATERIAL)
                            ) {
                                Text(text = stringResource(R.string.menu_import_material))
                            }
                            OutlinedButton(
                                onClick = { showSettingsSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(AuraNodeTestTags.MENU_OPEN_SETTINGS)
                            ) {
                                Text(text = stringResource(R.string.menu_open_settings))
                            }
                        }
                    }
                }
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuraFactChip(
                        text = stringResource(
                            R.string.menu_stat_questions_dynamic,
                            settings.questionsPerDifficulty
                        ),
                        compact = true
                    )
                    AuraFactChip(
                        text = stringResource(
                            R.string.menu_stat_themes_dynamic,
                            uiState.availableThemes.size
                        ),
                        accent = MaterialTheme.colorScheme.secondary,
                        compact = true
                    )
                    AuraFactChip(
                        text = stringResource(
                            R.string.menu_stat_atlas_dynamic,
                            uiState.unlockedAtlasNodes.size,
                            uiState.atlasNodes.size
                        ),
                        accent = MaterialTheme.colorScheme.tertiary,
                        compact = true
                    )
                    AuraFactChip(
                        text = stringResource(
                            R.string.menu_stat_packs_dynamic,
                            uiState.customPacks.size
                        ),
                        compact = true
                    )
                }
            }

            if (uiState.generationErrorMessage != null || uiState.generationWarnings.isNotEmpty()) {
                item {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.import_status_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            uiState.generationErrorMessage?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            uiState.generationWarnings.forEach { warning ->
                                Text(
                                    text = warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = onDismissGenerationMessage) {
                                Text(text = stringResource(R.string.common_dismiss))
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.menu_packs_title),
                    body = stringResource(R.string.menu_packs_body)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orderedPacks, key = { it.id }) { pack ->
                        QuizPackCard(
                            pack = pack,
                            selected = pack.id == uiState.selectedPackId,
                            onClick = { onPackSelected(pack.id) },
                            onDelete = if (pack.type == QuizPackType.CUSTOM_IMPORTED) {
                                { onDeletePack(pack.id) }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .width(if (compact) 260.dp else 290.dp)
                                .testTag(AuraNodeTestTags.packTag(pack.id))
                        )
                    }
                }
            }

            if (uiState.isOfficialPackSelected) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.menu_modes_title),
                        body = stringResource(R.string.menu_modes_body)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(QuizMode.entries) { mode ->
                            QuizModeCard(
                                mode = mode,
                                selected = uiState.selectedMode == mode,
                                enabled = mode != QuizMode.LEGEND || uiState.legendUnlockedDifficulties.isNotEmpty(),
                                onClick = { onModeSelected(mode) },
                                modifier = Modifier
                                    .width(230.dp)
                                    .testTag(AuraNodeTestTags.modeTag(mode))
                            )
                        }
                    }
                }
            } else {
                item {
                    AuraNodeSurfaceCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.menu_custom_pack_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.menu_custom_pack_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.menu_themes_title),
                    body = stringResource(R.string.menu_themes_body)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.availableThemes, key = { it.id }) { preset ->
                        ThemePresetCard(
                            preset = preset,
                            selected = preset.id == uiState.selectedThemeId,
                            onClick = { onThemeSelected(preset.id) },
                            modifier = Modifier
                                .width(if (compact) 216.dp else 236.dp)
                                .testTag(AuraNodeTestTags.themeTag(preset.id))
                        )
                    }
                }
            }

            item {
                AuraNodeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.menu_atlas_preview_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.menu_atlas_preview_body,
                                uiState.unlockedAtlasNodes.size,
                                uiState.atlasNodes.size
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.hallOfFameRuns.firstOrNull()?.let { bestRun ->
                            HallOfFameRunCard(rank = 1, runSummary = bestRun)
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = stringResource(
                        R.string.menu_levels_title_for_pack,
                        uiState.selectedPack?.title ?: stringResource(R.string.menu_title)
                    ),
                    body = stringResource(R.string.menu_levels_body)
                )
            }

            items(Difficulty.entries) { difficulty ->
                DifficultyRouteCard(
                    difficulty = difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    modifier = Modifier.testTag(AuraNodeTestTags.difficultyTag(difficulty)),
                    compact = compact
                )
            }
        }
    }

    if (uiState.shouldShowOnboarding) {
        ModalBottomSheet(onDismissRequest = {}) {
            OnboardingSheetContent(
                uiState = uiState,
                onHomePreferenceSelected = onHomePreferenceSelected,
                onAnswerModeSelected = onAnswerModeSelected,
                onCloudGenerationEnabled = onCloudGenerationEnabled,
                onComplete = onCompleteOnboarding
            )
        }
    }
}

@Composable
private fun LoadingMenuState(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag(AuraNodeTestTags.MENU_SCREEN),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            AuraNodeSurfaceCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.menu_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.startup_loading_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsSheetContent(
    uiState: QuizUiState,
    onTimerToggle: (Boolean) -> Unit,
    onCompactUiToggle: (Boolean) -> Unit,
    onShuffleToggle: (Boolean) -> Unit,
    onMotionToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onJuryModeToggle: (Boolean) -> Unit,
    onDemoResetOnLaunchToggle: (Boolean) -> Unit,
    onResetDemoProgress: () -> Unit,
    onAnswerModeSelected: (AnswerMode) -> Unit,
    onHomePreferenceSelected: (HomeContentPreference) -> Unit,
    onAiProviderSelected: (AiProvider) -> Unit,
    onCloudGenerationEnabled: (Boolean) -> Unit,
    onAiApiKeyChanged: (String) -> Unit,
    onGeminiModelChanged: (String) -> Unit,
    onOpenRouterModelChanged: (String) -> Unit
) {
    val settings = uiState.quizSettings
    val aiConfig = uiState.aiGenerationConfig
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.menu_settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = settings.showTimer,
                onClick = { onTimerToggle(!settings.showTimer) },
                label = { Text(text = stringResource(R.string.menu_setting_timer)) }
            )
            FilterChip(
                selected = settings.compactUi,
                onClick = { onCompactUiToggle(!settings.compactUi) },
                label = { Text(text = stringResource(R.string.menu_setting_compact)) }
            )
            FilterChip(
                selected = settings.shuffleQuestions,
                onClick = { onShuffleToggle(!settings.shuffleQuestions) },
                label = { Text(text = stringResource(R.string.menu_setting_shuffle)) }
            )
            FilterChip(
                selected = settings.motionEnabled,
                onClick = { onMotionToggle(!settings.motionEnabled) },
                label = { Text(text = stringResource(R.string.menu_setting_motion)) }
            )
            FilterChip(
                selected = settings.hapticsEnabled,
                onClick = { onHapticsToggle(!settings.hapticsEnabled) },
                label = { Text(text = stringResource(R.string.menu_setting_haptics)) }
            )
            FilterChip(
                selected = settings.soundEnabled,
                onClick = { onSoundToggle(!settings.soundEnabled) },
                label = { Text(text = stringResource(R.string.menu_setting_sound)) }
            )
            FilterChip(
                selected = settings.juryModeEnabled,
                onClick = { onJuryModeToggle(!settings.juryModeEnabled) },
                label = { Text(text = stringResource(R.string.menu_setting_jury_mode)) }
            )
            FilterChip(
                selected = settings.demoResetOnLaunch,
                onClick = { onDemoResetOnLaunchToggle(!settings.demoResetOnLaunch) },
                label = { Text(text = stringResource(R.string.menu_setting_demo_reset_on_launch)) }
            )
        }

        Text(
            text = stringResource(R.string.menu_setting_answer_mode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnswerMode.entries.forEach { answerMode ->
                FilterChip(
                    selected = settings.answerMode == answerMode,
                    onClick = { onAnswerModeSelected(answerMode) },
                    label = {
                        Text(
                            text = when (answerMode) {
                                AnswerMode.CLASSIC_OPTIONS -> stringResource(R.string.answer_mode_classic)
                                AnswerMode.EXPLORER_MIXED -> stringResource(R.string.answer_mode_mixed)
                                AnswerMode.EXPLORER_TEXT -> stringResource(R.string.answer_mode_text)
                            }
                        )
                    }
                )
            }
        }
        Text(
            text = stringResource(R.string.menu_home_preference_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeContentPreference.entries.forEach { preference ->
                FilterChip(
                    selected = settings.homeContentPreference == preference,
                    onClick = { onHomePreferenceSelected(preference) },
                    label = {
                        Text(
                            text = when (preference) {
                                HomeContentPreference.OFFICIAL_FIRST -> stringResource(R.string.home_preference_official)
                                HomeContentPreference.CUSTOM_FIRST -> stringResource(R.string.home_preference_custom)
                                HomeContentPreference.BALANCED -> stringResource(R.string.home_preference_balanced)
                            }
                        )
                    }
                )
            }
        }
        Text(
            text = stringResource(R.string.menu_ai_settings_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.menu_ai_cloud_toggle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = aiConfig.cloudGenerationEnabled,
                onCheckedChange = onCloudGenerationEnabled
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiProvider.entries.forEach { provider ->
                FilterChip(
                    selected = aiConfig.provider == provider,
                    onClick = { onAiProviderSelected(provider) },
                    label = {
                        Text(
                            text = when (provider) {
                                AiProvider.GEMINI -> stringResource(R.string.ai_provider_gemini)
                                AiProvider.OPENROUTER -> stringResource(R.string.ai_provider_openrouter)
                            }
                        )
                    }
                )
            }
        }
        OutlinedTextField(
            value = aiConfig.apiKey,
            onValueChange = onAiApiKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = stringResource(R.string.menu_ai_api_key)) }
        )
        if (aiConfig.provider == AiProvider.GEMINI) {
            OutlinedTextField(
                value = aiConfig.geminiModel,
                onValueChange = onGeminiModelChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(R.string.menu_ai_gemini_model)) }
            )
        } else {
            OutlinedTextField(
                value = aiConfig.openRouterModel,
                onValueChange = onOpenRouterModelChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(R.string.menu_ai_openrouter_model)) }
            )
        }
        Text(
            text = stringResource(R.string.menu_config_files_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onResetDemoProgress,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AuraNodeTestTags.MENU_RESET_PROGRESS)
        ) {
            Text(text = stringResource(R.string.menu_reset_progress))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingSheetContent(
    uiState: QuizUiState,
    onHomePreferenceSelected: (HomeContentPreference) -> Unit,
    onAnswerModeSelected: (AnswerMode) -> Unit,
    onCloudGenerationEnabled: (Boolean) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag(AuraNodeTestTags.MENU_ONBOARDING),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeContentPreference.entries.forEach { preference ->
                FilterChip(
                    selected = uiState.quizSettings.homeContentPreference == preference,
                    onClick = { onHomePreferenceSelected(preference) },
                    label = {
                        Text(
                            text = when (preference) {
                                HomeContentPreference.OFFICIAL_FIRST -> stringResource(R.string.home_preference_official)
                                HomeContentPreference.CUSTOM_FIRST -> stringResource(R.string.home_preference_custom)
                                HomeContentPreference.BALANCED -> stringResource(R.string.home_preference_balanced)
                            }
                        )
                    }
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnswerMode.entries.forEach { answerMode ->
                FilterChip(
                    selected = uiState.quizSettings.answerMode == answerMode,
                    onClick = { onAnswerModeSelected(answerMode) },
                    label = {
                        Text(
                            text = when (answerMode) {
                                AnswerMode.CLASSIC_OPTIONS -> stringResource(R.string.answer_mode_classic)
                                AnswerMode.EXPLORER_MIXED -> stringResource(R.string.answer_mode_mixed)
                                AnswerMode.EXPLORER_TEXT -> stringResource(R.string.answer_mode_text)
                            }
                        )
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.onboarding_ai_toggle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = uiState.aiGenerationConfig.cloudGenerationEnabled,
                onCheckedChange = onCloudGenerationEnabled
            )
        }
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AuraNodeTestTags.MENU_ONBOARDING_CONTINUE)
        ) {
            Text(text = stringResource(R.string.onboarding_continue))
        }
    }
}
