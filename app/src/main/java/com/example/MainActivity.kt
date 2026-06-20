package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.navigation.LauncherNavigation
import com.example.presentation.LauncherViewModel
import com.example.presentation.LauncherViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get repository from application container
        val appContainer = (applicationContext as LauncherApplication).container

        // Instantiate Shared ViewModel
        val viewModel: LauncherViewModel by viewModels {
            LauncherViewModelFactory(appContainer.repository)
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            // Initialize/sync installed applications upon startup
            LaunchedEffect(Unit) {
                viewModel.initializeApps(applicationContext)
            }

            // Material Theme reacts to Settings configurations!
            MyApplicationTheme(
                darkTheme = state.isDarkMode,
                themeName = state.themeName,
                fontName = state.themeFont,
                isGrayscale = state.isGrayscale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        
                        // Main Navigation Flow Content
                        val baseModifier = Modifier.fillMaxSize()
                        // Supports the adjusted settings Font Size multiplier!
                        CompositionLocalProvider(
                            LocalTextStyle provides LocalTextStyle.current.copy(
                                fontSize = LocalTextStyle.current.fontSize * state.fontSizeMultiplier
                            )
                        ) {
                            LauncherNavigation(
                                navController = navController,
                                state = state,
                                viewModel = viewModel,
                                modifier = baseModifier
                            )
                        }

                        // DIGITAL DETOX PAUSE FULLSCREEN ROADBLOCK OVERLAY
                        if (state.delayRemainingSeconds > 0 && state.pendingLaunchApp != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "MINDFULNESS BREAK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 2.sp
                                    )

                                    Spacer(modifier = Modifier.height(60.dp))

                                    // Countdown Large Typography
                                    Text(
                                        text = "${state.delayRemainingSeconds}",
                                        fontSize = 80.sp,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "Opening ${state.pendingLaunchApp?.label}...",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))

                                    // Centered Motivational Detox Quote
                                    Text(
                                        text = state.motivationalMessage,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(80.dp))

                                    OutlinedButton(
                                        onClick = { viewModel.cancelDetoxDelay() },
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text(
                                            text = "CANCEL LAUNCH & GO BACK",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
