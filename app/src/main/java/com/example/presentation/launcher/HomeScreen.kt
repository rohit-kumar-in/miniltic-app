package com.example.presentation.launcher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import com.example.domain.model.AppInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.LauncherUiState
import com.example.presentation.LauncherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onNavigateToApps: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var batteryPercent by remember { mutableStateOf(100) }

    // Reactively updates time
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            
            // Query battery status
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPercent = (level * 100) / scale
            }

            viewModel.checkDefaultLauncher(context)

            delay(1000)
        }
    }

    val favorites = state.allApps.filter { it.isFavorite }

    var showAddAppDialog by remember { mutableStateOf(false) }
    var selectedAppForLongPress by remember { mutableStateOf<AppInfo?>(null) }
    var showRenameDialogForApp by remember { mutableStateOf<AppInfo?>(null) }
    var homeSearchQuery by remember { mutableStateOf("") }

    val filteredFavorites = remember(favorites, homeSearchQuery) {
        if (homeSearchQuery.isEmpty()) {
            favorites
        } else {
            favorites.filter { (it.customLabel ?: it.label).contains(homeSearchQuery, ignoreCase = true) }
        }
    }

    val filteredOtherApps = remember(state.allApps, homeSearchQuery) {
        if (homeSearchQuery.isEmpty()) {
            emptyList()
        } else {
            state.allApps.filter { app ->
                !app.isFavorite && !app.isHidden && (app.customLabel ?: app.label).contains(homeSearchQuery, ignoreCase = true)
            }
        }
    }

    if (!state.onboardingCompleted) {
        OnboardingOverlay(
            state = state,
            viewModel = viewModel,
            onComplete = { viewModel.completeOnboarding() }
        )
    } else if (state.isDefaultLauncher && !state.isWizardCompleted) {
        SetupWizardOverlay(
            state = state,
            viewModel = viewModel
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 24.dp, horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // If not default launcher, show warning banner
                if (!state.isDefaultLauncher) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { promptSetDefaultLauncher(context) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set Launcher as Default Home App",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Unexpected Android close events will return to Stock Launcher if not default. Tap to set home.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            // Elegant replacement representation of traditional Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$batteryPercent%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 10.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(batteryPercent / 100f)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        )
                    }
                }
            }

            // Elegant Serif Clock & Date Widget (Centered with left constraint)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 12.dp, start = 8.dp)
            ) {
                val dayOfWeek = remember(currentDate) {
                    try {
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
                    } catch (e: Exception) {
                        "Today"
                    }
                }
                val formattedDate = remember(currentDate) {
                    try {
                        SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date())
                    } catch (e: Exception) {
                        currentDate
                    }
                }
                Text(
                    text = dayOfWeek,
                    fontSize = 44.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f),
                    letterSpacing = 1.sp
                )
            }

            // Center: Pinned Favorites text list (with weight to fill space)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FAVORITES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ ADD APP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showAddAppDialog = true }
                                .padding(4.dp)
                        )
                        Text(
                            text = "ALL APPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onNavigateToApps() }
                                .padding(4.dp)
                        )
                    }
                }

                if (favorites.isEmpty() && homeSearchQuery.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No apps selected",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { showAddAppDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Add App")
                            }
                            OutlinedButton(
                                onClick = { onNavigateToApps() },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Open All Apps")
                            }
                        }
                    }
                } else {
                    if (filteredFavorites.isEmpty() && filteredOtherApps.isEmpty()) {
                        Text(
                            text = "No applications match your search.",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (filteredFavorites.isNotEmpty()) {
                                if (state.gridColumns <= 1) {
                                    items(filteredFavorites) { app ->
                                        Text(
                                            text = app.customLabel ?: app.label,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            letterSpacing = (-0.5).sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .combinedClickable(
                                                    onClick = { viewModel.tryLaunchApp(context, app) },
                                                    onLongClick = { selectedAppForLongPress = app }
                                                )
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                } else {
                                    val chunked = filteredFavorites.chunked(state.gridColumns)
                                    items(chunked) { rowApps ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            rowApps.forEach { app ->
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .combinedClickable(
                                                            onClick = { viewModel.tryLaunchApp(context, app) },
                                                            onLongClick = { selectedAppForLongPress = app }
                                                        )
                                                        .padding(vertical = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    if (state.showAppIcons) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(48.dp)
                                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = (app.customLabel ?: app.label).take(1).uppercase(),
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    }
                                                    Text(
                                                        text = app.customLabel ?: app.label,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Light,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                            repeat(state.gridColumns - rowApps.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                            if (filteredOtherApps.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "OTHER APPLICATIONS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                                items(filteredOtherApps) { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { viewModel.tryLaunchApp(context, app) },
                                                onLongClick = { selectedAppForLongPress = app }
                                            )
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = app.customLabel ?: app.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        )
                                        Text(
                                            text = "TAP TO OPEN",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Apps Shortcut Carousel
                if (state.recentApps.isNotEmpty() && homeSearchQuery.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "RECENT ENTRIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.recentApps.take(4).forEach { recentApp ->
                            Text(
                                text = recentApp.customLabel ?: recentApp.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
                                modifier = Modifier
                                    .clickable { viewModel.tryLaunchApp(context, recentApp) }
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Screen Time / Digital Detox Widget (Exactly as in Elegant Dark specifications!)
            val totalUsageMs = state.appUsages.sumOf { it.screenTimeMs }
            val totalHours = totalUsageMs / 3600000
            val totalMinutes = (totalUsageMs % 3600000) / 60000
            val formattedTotalTime = if (totalUsageMs == 0L) "0m" else if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m"
            val productivityScore = viewModel.getProductivityScore()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "SCREEN TIME TODAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = formattedTotalTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "$productivityScore/100",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = if (productivityScore >= 70) Color(0xFF34D399) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Bar tracking productivity rating
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(2.2.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(productivityScore / 100f)
                            .background(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
                                shape = RoundedCornerShape(2.2.dp)
                            )
                    )
                }
            }

            // Real Interactive Search Bar Supporting Search Everywhere!
            OutlinedTextField(
                value = homeSearchQuery,
                onValueChange = { homeSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f)) },
                trailingIcon = {
                    if (homeSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { homeSearchQuery = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(27.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f),
                    focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            if (showAddAppDialog) {
                AddAppDialog(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { showAddAppDialog = false }
                )
            }

            selectedAppForLongPress?.let { app ->
                AlertDialog(
                    onDismissRequest = { selectedAppForLongPress = null },
                    title = {
                        Text(
                            text = app.customLabel ?: app.label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.toggleAppFavorite(app.packageName)
                                        selectedAppForLongPress = null
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (app.isFavorite) "Remove from Home" else "Add to Home",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showRenameDialogForApp = app
                                        selectedAppForLongPress = null
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rename App",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.toggleAppHidden(app.packageName)
                                        selectedAppForLongPress = null
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hide App",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = android.net.Uri.fromParts("package", app.packageName, null)
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        selectedAppForLongPress = null
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "App Info",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedAppForLongPress = null }) {
                            Text("CANCEL")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            showRenameDialogForApp?.let { app ->
                var newLabel by remember { mutableStateOf(app.customLabel ?: app.label) }
                AlertDialog(
                    onDismissRequest = { showRenameDialogForApp = null },
                    title = { Text("Rename App") },
                    text = {
                        OutlinedTextField(
                            value = newLabel,
                            onValueChange = { newLabel = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.renameAppLabel(app.packageName, if (newLabel.isBlank()) null else newLabel)
                            showRenameDialogForApp = null
                        }) {
                            Text("RENAME")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialogForApp = null }) {
                            Text("CANCEL")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Links Underneath
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = state.dockScale
                        scaleY = state.dockScale
                    }
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clickable { onNavigateToApps() }
                        .padding(8.dp)
                )

                Text(
                    text = "FOCUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clickable { onNavigateToFocus() }
                        .padding(8.dp)
                )

                Text(
                    text = "STATS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clickable { onNavigateToAnalytics() }
                        .padding(8.dp)
                )

                Text(
                    text = "SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clickable { onNavigateToSettings() }
                        .padding(8.dp)
                )
            }

            // Aesthetic Home Indicator Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
    }
}

// --- Onboarding & Helper Utilities ---

private fun hasNotificationAccess(context: Context): Boolean {
    val contentResolver = context.contentResolver
    val enabledListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return enabledListeners?.contains(context.packageName) == true
}

private fun promptSetDefaultLauncher(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

private fun promptUsageAccess(context: Context) {
    try {
        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun promptNotificationAccess(context: Context) {
    try {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun OnboardingOverlay(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var isNotificationEnabled by remember { mutableStateOf(hasNotificationAccess(context)) }

    // Periodically checks launcher and notification listener states while screen is open
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.checkDefaultLauncher(context)
            isNotificationEnabled = hasNotificationAccess(context)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "MINIMALIST LAUNCHER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to your digital detox.",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Configure these essential system roles to unlock your distraction-free container and prevent unexpected OS closes.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Step 1: Set Default Launcher
            item {
                OnboardingStepCard(
                    title = "1. SET AS DEFAULT LAUNCHER",
                    description = "Sets this screen as your active launcher. This prevents Android from exiting or closing the app after 1 minute of inactivity.",
                    isCompleted = state.isDefaultLauncher,
                    actionLabel = "SET DEFAULT HOME",
                    onAction = { promptSetDefaultLauncher(context) }
                )
            }

            // Step 2: Grant Usage Statistics
            item {
                OnboardingStepCard(
                    title = "2. ACTIVATE SCREEN TIME STATS",
                    description = "Permits access to generate local, safe screen time statistics on the home screen dynamically.",
                    isCompleted = state.hasUsageStatsPermission,
                    actionLabel = "ENABLE STATS",
                    onAction = { promptUsageAccess(context) }
                )
            }

            // Step 3: Enable Notification Blocker
            item {
                OnboardingStepCard(
                    title = "3. FILTER NOTIFICATIONS",
                    description = "Allows intercepting and batching notifications to block phone-ping stress triggers.",
                    isCompleted = isNotificationEnabled,
                    actionLabel = "SETUP NOTIFICATION ACCESS",
                    onAction = { promptNotificationAccess(context) }
                )
            }

            // Footer complete button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "FINISH ONBOARDING & ENTER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun OnboardingStepCard(
    title: String,
    description: String,
    isCompleted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                if (isCompleted) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = "PENDING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )

            if (!isCompleted) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(19.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun SetupWizardOverlay(
    state: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    
    // Define the 8 key roles
    val roles = remember {
        listOf(
            "Phone" to listOf("com.google.android.dialer", "com.android.dialer", "dialer", "phone"),
            "WhatsApp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
            "Messages" to listOf("com.google.android.apps.messaging", "com.android.messaging", "messaging", "message"),
            "Camera" to listOf("com.google.android.GoogleCamera", "com.android.camera", "camera"),
            "Chrome" to listOf("com.android.chrome", "chrome"),
            "Gmail" to listOf("com.google.android.gm", "gmail"),
            "Maps" to listOf("com.google.android.apps.maps", "maps"),
            "YouTube" to listOf("com.google.android.youtube", "youtube")
        )
    }

    // Resolve which package names match each role
    val matchedApps = remember(state.allApps) {
        roles.mapNotNull { (role, queries) ->
            val app = state.allApps.find { a ->
                val pkg = a.packageName.lowercase()
                val lbl = a.label.lowercase()
                queries.any { q -> pkg.contains(q) || lbl == q }
            }
            if (app != null) role to app else null
        }
    }

    // Keep track of which apps are selected
    val selectedPackages = remember(matchedApps) {
        mutableStateListOf<String>().apply {
            matchedApps.forEach { (role, app) ->
                // Automatically suggest Phone, Messages, Camera, Chrome, WhatsApp, Gmail, Maps if installed
                if (role != "YouTube") {
                    add(app.packageName)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to Minimal Launcher",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select the apps you use most to populate your distraction-free home screen:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (matchedApps.isEmpty()) {
                item {
                    Text(
                        text = "No standard apps detected on this system. You can add any app after entering.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                items(matchedApps) { (role, app) ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    selectedPackages.remove(app.packageName)
                                } else {
                                    selectedPackages.add(app.packageName)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = role,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = app.label,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) {
                                    selectedPackages.add(app.packageName)
                                } else {
                                    selectedPackages.remove(app.packageName)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.completeWizard(selectedPackages.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "CONFIRM & EXPLORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppDialog(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(state.allApps, searchQuery) {
        if (searchQuery.isEmpty()) {
            state.allApps
        } else {
            state.allApps.filter { (it.customLabel ?: it.label).contains(searchQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Apps to Home", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps...", fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No apps found", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    } else {
                        items(filteredApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleAppFavorite(app.packageName) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.customLabel ?: app.label,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = app.packageName,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Checkbox(
                                    checked = app.isFavorite,
                                    onCheckedChange = { viewModel.toggleAppFavorite(app.packageName) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

