package com.example.newapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.newapp.data.model.BackgroundArtworkStyle
import com.example.newapp.ui.components.AuraNodeBackdrop
import com.example.newapp.ui.quiz.QuizViewModel
import com.example.newapp.ui.screens.atlas.AtlasScreen
import com.example.newapp.ui.screens.materials.MaterialsScreen
import com.example.newapp.ui.screens.menu.MenuScreen
import com.example.newapp.ui.screens.quiz.QuizScreen
import com.example.newapp.ui.screens.result.ResultScreen
import com.example.newapp.ui.screens.settings.SettingsScreen
import com.example.newapp.ui.theme.AuraNodeTheme

@Composable
fun AuraNodeNavHost(
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(uiState.isQuizCompleted, currentRoute) {
        if (uiState.isQuizCompleted && currentRoute != AuraNodeDestination.Result.route) {
            navController.navigate(AuraNodeDestination.Result.route) {
                popUpTo(AuraNodeDestination.Quiz.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentRoute, uiState.questions.isEmpty()) {
        if (currentRoute == AuraNodeDestination.Quiz.route && uiState.questions.isEmpty()) {
            navController.navigate(AuraNodeDestination.Menu.route) {
                popUpTo(AuraNodeDestination.Menu.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    AuraNodeTheme(themePreset = uiState.selectedTheme) {
        AuraNodeBackdrop(
            modifier = modifier,
            backgroundStyle = uiState.selectedTheme?.backgroundStyle ?: BackgroundArtworkStyle.WAVES
        ) {
            NavHost(
                navController = navController,
                startDestination = AuraNodeDestination.Menu.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AuraNodeDestination.Menu.route) {
                    MenuScreen(
                        uiState = uiState,
                        onStartQuiz = {
                            viewModel.startSelectedPackQuiz()
                            navController.navigate(AuraNodeDestination.Quiz.route) {
                                launchSingleTop = true
                            }
                        },
                        onDifficultySelected = viewModel::setSelectedDifficulty,
                        onModeSelected = viewModel::selectMode,
                        onOpenAtlas = {
                            navController.navigate(AuraNodeDestination.Atlas.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenMaterials = {
                            navController.navigate(AuraNodeDestination.Materials.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSettings = {
                            navController.navigate(AuraNodeDestination.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AuraNodeDestination.Settings.route) {
                    SettingsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onThemeSelected = viewModel::selectTheme,
                        onTimerEnabledChanged = viewModel::setTimerEnabled,
                        onTimerSecondsChanged = viewModel::setTimerSeconds,
                        onQuestionsCountChanged = viewModel::setQuestionsPerDifficulty,
                        onShuffleQuestionsChanged = viewModel::setShuffleQuestionsEnabled,
                        onShuffleOptionsChanged = viewModel::setShuffleOptionsEnabled,
                        onCompactUiChanged = viewModel::setCompactUiEnabled,
                        onMotionChanged = viewModel::setMotionEnabled,
                        onHapticsChanged = viewModel::setHapticsEnabled,
                        onSoundChanged = viewModel::setSoundEnabled,
                        onDeleteAllCustomPacks = viewModel::deleteAllCustomPacks,
                        onClearProgress = viewModel::clearResultsAndProgress
                    )
                }

                composable(AuraNodeDestination.Materials.route) {
                    MaterialsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onImportDocumentSelected = viewModel::prepareImportedDocument,
                        onDifficultySelected = viewModel::setSelectedDifficulty,
                        onStartPackQuiz = { packId, difficulty ->
                            viewModel.startPackQuiz(packId, difficulty)
                            navController.navigate(AuraNodeDestination.Quiz.route) {
                                launchSingleTop = true
                            }
                        },
                        onDeletePack = viewModel::deleteCustomPack,
                        onDeleteAllCustomPacks = viewModel::deleteAllCustomPacks,
                        onDismissMessage = viewModel::dismissMessage,
                        onClearDraft = viewModel::clearImportedDraft,
                        onRebuildDraftQuestions = viewModel::rebuildImportedDraftQuestions,
                        onDraftTitleChanged = viewModel::updateDraftTitle,
                        onDraftDescriptionChanged = viewModel::updateDraftDescription,
                        onToggleSection = viewModel::toggleDraftSection,
                        onAddDraftQuestion = viewModel::addDraftQuestion,
                        onRemoveDraftQuestion = viewModel::removeDraftQuestion,
                        onDraftQuestionTextChanged = viewModel::updateDraftQuestionText,
                        onDraftQuestionOptionChanged = viewModel::updateDraftQuestionOption,
                        onDraftQuestionCorrectAnswerChanged = viewModel::updateDraftQuestionCorrectAnswer,
                        onDraftQuestionDifficultyChanged = viewModel::updateDraftQuestionDifficulty,
                        onDraftQuestionExplanationChanged = viewModel::updateDraftQuestionExplanation,
                        onSaveDraft = viewModel::saveImportedDraft
                    )
                }

                composable(AuraNodeDestination.Atlas.route) {
                    AtlasScreen(
                        uiState = uiState,
                        onBackToMenu = { navController.popBackStack() },
                        onSelectAtlasNode = viewModel::selectAtlasNode,
                        onFocusLatestUnlock = viewModel::focusLatestUnlockedAtlasNode,
                        onAtlasPanelModeChanged = viewModel::setAtlasPanelMode
                    )
                }

                composable(AuraNodeDestination.Quiz.route) {
                    QuizScreen(
                        uiState = uiState,
                        onAnswerSelected = viewModel::submitAnswer,
                        onReturnToMenu = {
                            viewModel.resetQuiz()
                            navController.navigate(AuraNodeDestination.Menu.route) {
                                popUpTo(AuraNodeDestination.Menu.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AuraNodeDestination.Result.route) {
                    ResultScreen(
                        uiState = uiState,
                        onBackToMenu = {
                            viewModel.resetQuiz()
                            navController.navigate(AuraNodeDestination.Menu.route) {
                                popUpTo(AuraNodeDestination.Menu.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onRetryDifficulty = {
                            viewModel.restartCurrentDifficulty()
                            navController.navigate(AuraNodeDestination.Quiz.route) {
                                popUpTo(AuraNodeDestination.Result.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onOpenAtlas = {
                            navController.navigate(AuraNodeDestination.Atlas.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
