package com.example.presentation.search

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.AppInfo
import com.example.presentation.LauncherUiState
import com.example.presentation.LauncherViewModel

@Composable
fun NativeAppIcon(packageName: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            android.widget.ImageView(ctx).apply {
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
        },
        update = { imageView ->
            try {
                val iconDrawable = imageView.context.packageManager.getApplicationIcon(packageName)
                imageView.setImageDrawable(iconDrawable)
            } catch (e: Exception) {
                // system default icon if package isn't resolved
                imageView.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedAppForConfig by remember { mutableStateOf<AppInfo?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search Input Header
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...", color = MaterialTheme.colorScheme.secondary) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            // Recent Searches Chips
            if (state.recentSearches.isNotEmpty() && state.searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SEARCHES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CLEAR",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearRecentSearches() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.recentSearches) { q ->
                        SuggestionChip(
                            onClick = { viewModel.onSearchQueryChanged(q) },
                            label = { Text(q, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.onBackground,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // Recent Apps Quick Row inside Search screen
            if (state.recentApps.isNotEmpty() && state.searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "RECENTLY LAUNCHED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.recentApps.take(4).forEach { app ->
                        AssistChip(
                            onClick = { viewModel.tryLaunchApp(context, app) },
                            label = { Text(app.customLabel ?: app.label, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onBackground,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Count & Back Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALL APPLICATIONS (${state.searchResults.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
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

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable text-only list
            if (state.searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No applications found.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.searchResults) { app ->
                        var currentLetter = (app.customLabel ?: app.label).firstOrNull()?.uppercaseChar() ?: '#'
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        viewModel.addRecentSearch(state.searchQuery)
                                        viewModel.tryLaunchApp(context, app)
                                    },
                                    onLongClick = {
                                        selectedAppForConfig = app
                                    }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.showAppIcons) {
                                NativeAppIcon(
                                    packageName = app.packageName,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(end = 12.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.customLabel ?: app.label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (app.openingDelaySeconds > 0) {
                                    Text(
                                        text = "Delay active (${app.openingDelaySeconds}s)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (app.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = "Pinned App",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (app.isWhitelisted) {
                                    Text(
                                        text = "WHITELIST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Preferences configurator Dialog
    selectedAppForConfig?.let { app ->
        // Query current app properties reactively
        val freshApp = state.allApps.find { it.packageName == app.packageName } ?: app
        
        AlertDialog(
            onDismissRequest = { selectedAppForConfig = null },
            title = {
                Text(
                    text = freshApp.customLabel ?: freshApp.label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = freshApp.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Favorites Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAppFavorite(freshApp.packageName) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Favorite App", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Show on minimalist home screen", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = freshApp.isFavorite,
                            onCheckedChange = { viewModel.toggleAppFavorite(freshApp.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Whitelist Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAppWhitelisted(freshApp.packageName) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Whitelisted from Focus", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Always allowed to launch", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = freshApp.isWhitelisted,
                            onCheckedChange = { viewModel.toggleAppWhitelisted(freshApp.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Hidden Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAppHidden(freshApp.packageName) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hide Application", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Conceal from listing", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = freshApp.isHidden,
                            onCheckedChange = { viewModel.toggleAppHidden(freshApp.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Custom Delay Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("App-Specific Delay Interval", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text("Add screen pause to break automatic opening", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val delays = listOf(0, 5, 10, 15, 30, 60)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(delays) { delayValue ->
                                val selected = freshApp.openingDelaySeconds == delayValue
                                OutlinedButton(
                                    onClick = { viewModel.setAppOpeningDelay(freshApp.packageName, delayValue) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (delayValue == 0) "None" else "${delayValue}s", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAppForConfig = null }) {
                    Text("DONE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
