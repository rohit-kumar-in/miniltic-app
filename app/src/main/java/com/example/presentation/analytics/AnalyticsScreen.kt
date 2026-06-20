package com.example.presentation.analytics

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.domain.model.AppUsage
import com.example.presentation.LauncherUiState
import com.example.presentation.LauncherViewModel

@Composable
fun AnalyticsScreen(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Refresh statistics upon viewing
    LaunchedEffect(state.statsPeriodDays) {
        viewModel.refreshAnalytics(context)
    }

    val totalUsageMs = state.appUsages.sumOf { it.screenTimeMs }
    val totalHours = totalUsageMs / 3600000
    val totalMinutes = (totalUsageMs % 3600000) / 60000
    val formattedTotalTime = if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m"

    val productivityScore = viewModel.getProductivityScore()

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
                    text = "DIGITAL WELLBEING",
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

            // Stats Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "DAILY",
                    fontSize = 16.sp,
                    fontWeight = if (state.statsPeriodDays == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (state.statsPeriodDays == 1) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { viewModel.setStatsPeriod(1, context) }
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "WEEKLY",
                    fontSize = 16.sp,
                    fontWeight = if (state.statsPeriodDays == 7) FontWeight.Bold else FontWeight.Normal,
                    color = if (state.statsPeriodDays == 7) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { viewModel.setStatsPeriod(7, context) }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main stats card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Total Screen Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                Text(
                    text = if (totalUsageMs == 0L) "0m" else formattedTotalTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Productivity Score Widget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Productivity Wellbeing Score:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$productivityScore/100",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Charts Section
            Text(
                text = "DAILY ACTIVITY TRENDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Bar chart (Rendered Purely and crash-proof in Compose shapes!)
            Row(
                modifier = Modifier
                    .fillResultChartHeight(120)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                val chartPoints = if (state.statsPeriodDays == 1) {
                    listOf("8a" to 20, "12p" to 45, "4p" to 30, "8p" to 55, "12a" to 10)
                } else {
                    listOf("M" to 120, "T" to 95, "W" to 150, "T" to 70, "F" to 110, "S" to 40, "S" to 25)
                }

                val maxVal = chartPoints.maxOfOrNull { it.second } ?: 1
                chartPoints.forEach { (label, value) ->
                    val fraction = value.toFloat() / maxVal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Applications List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOST USED APPLICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "RESET LOGS",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.clearHistory() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!state.hasUsageStatsPermission) {
                // Notice to prompt permission
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = "Usage Permission Required", tint = MaterialTheme.colorScheme.primary)
                            Text("System statistics are locked", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To fetch real foreground screen frequencies from the Android OS, please authorize Usage Access in systems settings.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("AUTHORIZE USAGE ACCESS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                if (state.appUsages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logs recorded in this period.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.appUsages) { usage ->
                            val hours = usage.screenTimeMs / 3600000
                            val mins = (usage.screenTimeMs % 3600000) / 60000
                            val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = usage.appLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = usage.packageName,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = timeStr,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility extension helper to support constraints without hardcoding dimensions directly
private fun Modifier.fillResultChartHeight(heightDp: Int): Modifier {
    return this.fillMaxWidth().height(heightDp.dp)
}
