package com.example.presentation.settings

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

@Composable
fun SettingsScreen(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showWhitelistDialog by remember { mutableStateOf(false) }

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
                    text = "LAUNCHER SETTINGS",
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

            Spacer(modifier = Modifier.height(20.dp))

            // Settings scroll container
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                
                // Item 1: Theme Mode toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDarkMode() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Dark Theme", tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text("Aesthetic Theme", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(if (state.isDarkMode) "Pure Distraction-Free Dark" else "Eye-Safe Light", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Switch(
                            checked = state.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Item 2: Show/Hide App Icons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleShowAppIcons() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "App Icons Display", tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text("Show Installed App Icons", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Hiding icons decreases immediate brain urges", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Switch(
                            checked = state.showAppIcons,
                            onCheckedChange = { viewModel.toggleShowAppIcons() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Item 3: Font Size multiplier selection
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Font settings", tint = MaterialTheme.colorScheme.secondary)
                            Text("Text Size Adjustment", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

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

                // Item 4: Global detox loading pause delay
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Pause screen settings", tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text("Global Opening Detox Delay", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Puts a mindfulness pause before launching any app", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
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

                // Item 5: Whitelisted applications Dialog trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWhitelistDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Whitelist Selector", tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text("Exempt/Whitelisted Applications", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                val count = state.allApps.count { it.isWhitelisted }
                                Text("$count applications allowed to skip detox delays", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Text("EDIT →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Item 6: Batch Notification management
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleBatchNotifications() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Batched Alerts", tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text("Batch Notifications summary", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Suppress and digest notifications in batches", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
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

                // Item 7: Backup/Restore triggers (File saving)
                item {
                    Text(
                        text = "MAINTENANCE & RECOVERY",
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
                                val message = viewModel.requestBackup(context)
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = "Backup settings", modifier = Modifier.size(16.dp))
                                Text("BACKUP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Restore button
                        OutlinedButton(
                            onClick = {
                                val message = viewModel.requestRestore(context)
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = "Restore settings", modifier = Modifier.size(16.dp))
                                Text("RESTORE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Whitelist Selector nested dialogue
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

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

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
}
