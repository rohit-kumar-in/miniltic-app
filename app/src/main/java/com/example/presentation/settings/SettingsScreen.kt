package com.example.presentation.settings

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppInfo
import com.example.presentation.LauncherUiState
import com.example.presentation.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showWhitelistDialog by remember { mutableStateOf(false) }
    var showAppManagerDialog by remember { mutableStateOf(false) }
    var showSchedulesDialog by remember { mutableStateOf(false) }
    var showNotificationDigestDialog by remember { mutableStateOf(false) }
    var showRestoreSchemaDialog by remember { mutableStateOf(false) }
    var inputRestoreJson by remember { mutableStateOf("") }
    
    // For App Manager Dialogue
    var selectedAppToEdit by remember { mutableStateOf<AppInfo?>(null) }
    var tempCustomLabel by remember { mutableStateOf("") }
    var tempTimeLimitMinutes by remember { mutableStateOf("") }

    // For Schedules
    var sleepStart by remember { mutableStateOf(state.sleepStartTime) }
    var sleepEnd by remember { mutableStateOf(state.sleepEndTime) }
    var sleepActive by remember { mutableStateOf(state.sleepModeActive) }
    var workStart by remember { mutableStateOf(state.workStartTime) }
    var workEnd by remember { mutableStateOf(state.workEndTime) }
    var workActive by remember { mutableStateOf(state.workModeActive) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAUNCHER DETOX CONFIG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "← BACK TO HOME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings scroll container
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                
                // PREMIUM SUBSCRIPTION SECTION
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (state.isPremiumUser) "★ DEEP WORK DETOX PREMIUM" else "★ DETOX BASIC TRIAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isPremiumUser) {
                                    "Premium membership active. Enjoy limitless schedules, premium themes, and infinite Pomodoros!"
                                } else {
                                    "Free trial: ${state.trialDaysRemaining} days remaining. Upgrade today to unlock infinite customized layouts."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!state.isPremiumUser) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.initiatePremiumSubscriptionFlow(context, "premium_lifetime") { success ->
                                                if (success) {
                                                    Toast.makeText(context, "Premium Lifetime Subscription Activated via Google Play Billing!", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Go Premium (One-Time)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.initiatePremiumSubscriptionFlow(context, "premium_monthly") { success ->
                                                if (success) {
                                                    Toast.makeText(context, "Premium Monthly Subscription Subscribed via Google Play Billing!", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Subscribe Monthly", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "AESTHETICS & BRANDING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                    )
                }

                // Wellness Wallpaper trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, com.example.presentation.launcher.WellnessWallpaperActivity::class.java)
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wellness Wallpaper System", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Dynamic wallpapers based on your current well-being states", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("CONFIGURE →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Custom Themes Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Color Palette Theme", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        val themes = listOf("Charcoal", "Forest", "Sunset", "Indigo")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(themes) { t ->
                                val active = state.themeName.equals(t, ignoreCase = true)
                                OutlinedButton(
                                    onClick = { viewModel.setCustomTheme(t) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Custom Font Selector Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Custom Font Styles & Packs", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        val fonts = listOf(
                            "SansSerif" to "Sans", 
                            "Monospace" to "Mono", 
                            "Serif" to "Serif",
                            "cursive" to "Cursive",
                            "sans-serif-condensed" to "Condensed",
                            "sans-serif-black" to "Black"
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fonts.forEach { (key, display) ->
                                val active = state.themeFont.equals(key, ignoreCase = true)
                                OutlinedButton(
                                    onClick = { viewModel.setCustomFont(key) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    Text(display, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Grid Size Customization Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Custom Grid Layout Columns", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 4).forEach { cols ->
                                val active = state.gridColumns == cols
                                OutlinedButton(
                                    onClick = { viewModel.setGridColumns(cols) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text(if (cols == 1) "List" else "${cols}x${cols}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Dock Scale Selection Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Navigation Bottom Dock Scale", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.8f, 1.0f, 1.2f, 1.4f).forEach { scale ->
                                val active = state.dockScale == scale
                                OutlinedButton(
                                    onClick = { viewModel.setDockScale(scale) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text("${(scale * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Grayscale Mode Selector Toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleGrayscale() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Grayscale Screen Filter", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Dulls system vibrancy to reduce addictive loop cravings", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = state.isGrayscale,
                            onCheckedChange = { viewModel.toggleGrayscale() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Text size multiplier
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Text Size Adjustment", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        val sizes = listOf(0.85f to "Fine", 1.0f to "Standard", 1.15f to "Comfort", 1.3f to "Giga")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sizes.forEach { (mult, label) ->
                                val active = state.fontSizeMultiplier == mult
                                OutlinedButton(
                                    onClick = { viewModel.setFontSizeMultiplier(mult) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "FOCUS BOUNDARIES & TIME GOALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                    )
                }

                // App Organizer dialogue trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAppManagerDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Rename Apps & Set Limits", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Alter labels or define maximum daily open durations", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("MANAGE →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Focus boundaries / schedules dialogue trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSchedulesDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Blocking Schedules", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Work hours mode, bed sleep limits and weekend rules", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("Schedules →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Item 4: Global delay pause configuration
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text("Global Launch Mindfulness Pause", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Puts an active digital timeout delay before any non-whitelisted launch", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        val delays = listOf(0, 5, 15, 30, 45, 60)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(delays) { sec ->
                                val active = state.globalDelaySeconds == sec
                                OutlinedButton(
                                    onClick = { viewModel.setGlobalDelay(sec) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (sec == 0) "None" else "${sec}s", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Whitelisted applications Dialog trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWhitelistDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Exempt Whitelisted Apps", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            val count = state.allApps.count { it.isWhitelisted }
                            Text("$count applications bypass detox schedules and delay pauses", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("EDIT →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Dynamic Notification batch digest options
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNotificationDigestDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Notification Digest & Summary", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            val totalIntercepted = state.notificationLogs.size
                            Text("$totalIntercepted batched alerts categorized & summarized", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("VIEW DIGEST →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Show batch notification toggle block
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleBatchNotifications() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Suppress Notifications Instantly", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Redirect system alerts to Batched digest summary", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = state.batchNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleBatchNotifications() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                item {
                    Text(
                        text = "MAINTENANCE & DATABASE RECOVERY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Backup button
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportSettingsJson()
                                try {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Minimal Launcher Settings Backup", json)
                                    clipboard.setPrimaryClip(clip)
                                    // Also save locally as a file
                                    context.openFileOutput("launcher_backup_v4.json", android.content.Context.MODE_PRIVATE).use {
                                        it.write(json.toByteArray())
                                    }
                                    Toast.makeText(context, "Backup JSON Schema copied to clipboard and saved to local file!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("EXPORT SCHEMA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Restore button
                        OutlinedButton(
                            onClick = {
                                showRestoreSchemaDialog = true
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("IMPORT / RESTORE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showRestoreSchemaDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSchemaDialog = false },
            title = { Text("Restore Settings Schema", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your exported configuration JSON backup below to validate and overwrite system settings:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    OutlinedTextField(
                        value = inputRestoreJson,
                        onValueChange = { inputRestoreJson = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("{\n  \"version\": 4,\n  \"signature\": \"...\"\n}") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val (success, text) = viewModel.restoreFromJson(inputRestoreJson)
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                        if (success) {
                            showRestoreSchemaDialog = false
                            inputRestoreJson = ""
                        }
                    }
                ) {
                    Text("VALIDATE & RESTORE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreSchemaDialog = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Interactive Whitelist Selector dialogue
    if (showWhitelistDialog) {
        AlertDialog(
            onDismissRequest = { showWhitelistDialog = false },
            title = {
                Text(
                    text = "Configure Whitelists",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    Text(
                        text = "Check applications to exempt them from any delay popups or focus blockers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.allApps.filter { !it.isHidden }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleAppWhitelisted(app.packageName) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                              ) {
                                Text(app.label, fontSize = 14.sp)
                                Checkbox(
                                    checked = app.isWhitelisted,
                                    onCheckedChange = { viewModel.toggleAppWhitelisted(app.packageName) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhitelistDialog = false }) {
                    Text("CLOSE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // App Manager Dialog (Rename Labels & Set Time Limits)
    if (showAppManagerDialog) {
        AlertDialog(
            onDismissRequest = { showAppManagerDialog = false },
            title = {
                Text(
                    text = "App Organizer & Limits",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    if (selectedAppToEdit == null) {
                        Text(
                            text = "Choose an application to rename or define a maximum daily usage limit.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.allApps) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedAppToEdit = app
                                            tempCustomLabel = app.customLabel ?: app.label
                                            tempTimeLimitMinutes = if (app.limitMinutes > 0) app.limitMinutes.toString() else ""
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(app.label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        if (app.customLabel != null) {
                                            Text("Renamed from original", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Text(
                                        text = if (app.limitMinutes > 0) "${app.limitMinutes} min limit" else "No limit",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        // Edit detail panel
                        val app = selectedAppToEdit!!
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Editing: ${app.packageName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            
                            OutlinedTextField(
                                value = tempCustomLabel,
                                onValueChange = { tempCustomLabel = it },
                                label = { Text("App Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempTimeLimitMinutes,
                                onValueChange = { tempTimeLimitMinutes = it },
                                label = { Text("Daily Time Limit (minutes)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.renameAppLabel(app.packageName, tempCustomLabel.ifBlank { null })
                                        val mins = tempTimeLimitMinutes.toIntOrNull() ?: 0
                                        viewModel.setAppDurationLimit(app.packageName, mins)
                                        Toast.makeText(context, "Saved app configurations!", Toast.LENGTH_SHORT).show()
                                        selectedAppToEdit = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("SAVE")
                                }
                                OutlinedButton(
                                    onClick = { selectedAppToEdit = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("BACK")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedAppToEdit == null) {
                    TextButton(onClick = { showAppManagerDialog = false }) {
                        Text("CLOSE")
                    }
                }
            }
        )
    }

    // Schedules Dialog (Sleep / Work Hour boundaries & Weekend rules)
    if (showSchedulesDialog) {
        AlertDialog(
            onDismissRequest = { showSchedulesDialog = false },
            title = { Text("Focus Schedules & Limits", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    // Daily focus goal minutes
                    var dailyGoalStr by remember { mutableStateOf(state.dailyGoalMinutes.toString()) }
                    OutlinedTextField(
                        value = dailyGoalStr,
                        onValueChange = { 
                            dailyGoalStr = it
                            it.toIntOrNull()?.let { mins -> viewModel.updateDailyGoal(mins) }
                        },
                        label = { Text("Daily Focus Goal (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // Sleep Mode Options
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sleep Mode Detoxing", fontWeight = FontWeight.Medium)
                            Switch(checked = sleepActive, onCheckedChange = { sleepActive = it })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sleepStart,
                                onValueChange = { sleepStart = it },
                                label = { Text("Starts") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sleepEnd,
                                onValueChange = { sleepEnd = it },
                                label = { Text("Ends") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Work hours
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Work Hours Distractions Block", fontWeight = FontWeight.Medium)
                            Switch(checked = workActive, onCheckedChange = { workActive = it })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = workStart,
                                onValueChange = { workStart = it },
                                label = { Text("Starts") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = workEnd,
                                onValueChange = { workEnd = it },
                                label = { Text("Ends") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Weekend Rules Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekend Offline Rule", fontWeight = FontWeight.Medium)
                            Text("Block addictive launch apps automatically on weekends", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(checked = state.weekendRulesActive, onCheckedChange = { viewModel.toggleWeekendRules() })
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateSleepSchedule(sleepStart, sleepEnd, sleepActive)
                        viewModel.updateWorkSchedule(workStart, workEnd, workActive)
                        Toast.makeText(context, "Schedules initialized successfully!", Toast.LENGTH_SHORT).show()
                        showSchedulesDialog = false
                    }
                ) {
                    Text("APPLY & CLOSE")
                }
            }
        )
    }

    // Dynamic Batched Notification Digest Summary Dialog
    if (showNotificationDigestDialog) {
        val workNotifs = state.notificationLogs.filter { it.category.equals("Work", ignoreCase = true) }
        val socialNotifs = state.notificationLogs.filter { it.category.equals("Social", ignoreCase = true) }
        val personalNotifs = state.notificationLogs.filter { it.category.equals("Personal", ignoreCase = true) }
        val generalNotifs = state.notificationLogs.filter { it.category.equals("General", ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showNotificationDigestDialog = false },
            title = { Text("Batched Notification Digest", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "To minimize dopamine spikes, alerts are batched into categorized lists. Here is your summarized digest.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "SMART SUMMARY GENERATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("AI Digest Summary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (state.notificationLogs.isEmpty()) {
                                    "No intercepted notifications to summarize. Your morning workspace is clean!"
                                } else {
                                    "You missed ${socialNotifs.size} social threads (mostly chat alerts) and ${workNotifs.size} work-related digests. Relax. All priorities are safe!"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AI Productivity Coach Alert:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = viewModel.getAIProductivityCoachAdvice(),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Daily Mental Coaching Trend:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = viewModel.getDailyInsights(),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider()

                    // List counts
                    Text("Alert Categories:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("✉ WORK ENTRIES (${workNotifs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (workNotifs.isNotEmpty()) "Pending digest" else "Empty", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("💬 SOCIAL PLATFORMS (${socialNotifs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (socialNotifs.isNotEmpty()) "Pending sync" else "Empty", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("👤 PERSONAL INCOMING (${personalNotifs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (personalNotifs.isNotEmpty()) "Batched" else "Empty", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("⚙ GENERAL NOTIFICATIONS (${generalNotifs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (generalNotifs.isNotEmpty()) "Intercepted" else "Empty", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.clearNotifications()
                            Toast.makeText(context, "Notification Digest cleared successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("CLEAR HISTORY DIGEST", fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDigestDialog = false }) {
                    Text("CLOSE")
                }
            }
        )
    }
}
