package com.example.newapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.newapp.R
import com.example.newapp.data.model.BackgroundArtworkStyle
import com.example.newapp.ui.components.AuraNodeBackdrop
import com.example.newapp.ui.quiz.QuizViewModel
import com.example.newapp.ui.screens.atlas.AtlasScreen
import com.example.newapp.ui.screens.menu.MenuScreen
import com.example.newapp.ui.screens.quiz.QuizScreen
import com.example.newapp.ui.screens.result.ResultScreen
import com.example.newapp.ui.share.ResultShareManager
import com.example.newapp.ui.theme.AuraNodeTheme

@Composable
fun AuraNodeNavHost(
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
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
                        onDifficultySelected = { difficulty ->
                            viewModel.startQuiz(difficulty)
                            navController.navigate(AuraNodeDestination.Quiz.route) {
                                launchSingleTop = true
                            }
                        },
                        onModeSelected = viewModel::selectMode,
                        onPackSelected = viewModel::selectPack,
                        onDeletePack = viewModel::deleteCustomPack,
                        onImportDocumentSelected = viewModel::prepareImportedDocument,
                        onGenerateCloudPack = viewModel::requestCloudPackGeneration,
                        onGenerateOfflinePack = viewModel::generateOfflinePack,
                        onDismissImportPreview = viewModel::dismissImportedDocumentPreview,
                        onConfirmAiConsent = viewModel::confirmCloudConsentAndGenerate,
                        onDismissAiConsent = viewModel::dismissAiConsent,
                        onDismissGenerationMessage = viewModel::dismissGenerationMessage,
                        onThemeSelected = viewModel::selectTheme,
                        onTimerToggle = viewModel::setTimerEnabled,
                        onCompactUiToggle = viewModel::setCompactUiEnabled,
                        onShuffleToggle = viewModel::setShuffleQuestionsEnabled,
                        onMotionToggle = viewModel::setMotionEnabled,
                        onHapticsToggle = viewModel::setHapticsEnabled,
                        onSoundToggle = viewModel::setSoundEnabled,
                        onJuryModeToggle = viewModel::setJuryModeEnabled,
                        onDemoResetOnLaunchToggle = viewModel::setDemoResetOnLaunch,
                        onResetDemoProgress = viewModel::resetDemoProgress,
                        onAnswerModeSelected = viewModel::setAnswerMode,
                        onHomePreferenceSelected = viewModel::setHomeContentPreference,
                        onAiProviderSelected = viewModel::setAiProvider,
                        onCloudGenerationEnabled = viewModel::setCloudGenerationEnabled,
                        onAiApiKeyChanged = viewModel::updateAiApiKey,
                        onGeminiModelChanged = viewModel::updateGeminiModel,
                        onOpenRouterModelChanged = viewModel::updateOpenRouterModel,
                        onCompleteOnboarding = viewModel::completeOnboarding,
                        onOpenAtlas = {
                            navController.navigate(AuraNodeDestination.Atlas.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AuraNodeDestination.Atlas.route) {
                    AtlasScreen(
                        uiState = uiState,
                        onBackToMenu = {
                            navController.navigate(AuraNodeDestination.Menu.route) {
                                popUpTo(AuraNodeDestination.Menu.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AuraNodeDestination.Quiz.route) {
                    QuizScreen(
                        uiState = uiState,
                        onAnswerSelected = viewModel::submitAnswer,
                        onAnswerInputChanged = viewModel::updateAnswerInput,
                        onSubmitTypedAnswer = viewModel::submitTypedAnswer,
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
                        },
                        onShareResult = { runSummary ->
                            ResultShareManager.shareResult(
                                context = context,
                                runSummary = runSummary,
                                themePreset = uiState.selectedTheme,
                                highlightFact = uiState.unlockedAtlasNodes.lastOrNull()?.highlightFact
                                    ?: context.getString(R.string.result_share_fallback_fact)
                            )
                        }
                    )
                }
            }
        }
    }
}
