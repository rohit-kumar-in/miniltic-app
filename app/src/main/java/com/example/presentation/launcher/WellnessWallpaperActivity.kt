package com.example.presentation.launcher

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LauncherApplication
import com.example.domain.model.AppUsage
import com.example.domain.model.WellnessState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WellnessWallpaperActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WellnessWallpaperScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessWallpaperScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var autoMode by remember { mutableStateOf(true) }
    var manualCategory by remember { mutableStateOf("Balanced") }
    val repo = (context.applicationContext as LauncherApplication).container.repository

    // Fetch initial state
    LaunchedEffect(Unit) {
        autoMode = repo.getSetting("wallpaper_auto_mode")?.toBoolean() ?: true
        manualCategory = repo.getSetting("wallpaper_category") ?: "Balanced"
    }

    var showPreview by remember { mutableStateOf<String?>(null) }
    var insightMessage by remember { mutableStateOf("") }
    
    // Evaluate wellness state
    var currentWellnessState by remember { mutableStateOf(WellnessState.Unknown) }
    
    LaunchedEffect(Unit) {
        val usages = repo.getRealAppUsage(context, 1)
        val totalMs = usages.sumOf { it.screenTimeMs }
        val focusSessions = repo.observeFocusSessions().firstOrNull() ?: emptyList()
        val completedFocusMins = focusSessions.filter { it.completed }.sumOf { it.durationMinutes }
        
        // Determine state
        val socialMediaMs = usages.filter { 
            it.packageName.contains("facebook") || it.packageName.contains("tiktok") || it.packageName.contains("instagram") 
        }.sumOf { it.screenTimeMs }

        currentWellnessState = when {
            totalMs > 4 * 60 * 60 * 1000L -> WellnessState.Overloaded
            socialMediaMs > 2 * 60 * 60 * 1000L -> WellnessState.Distracted
            completedFocusMins > 60 -> WellnessState.Focused
            totalMs < 1 * 60 * 60 * 1000L -> WellnessState.Mindful
            else -> WellnessState.Balanced
        }
        
        insightMessage = "Current State: $currentWellnessState\nTotal Screen Time: ${totalMs / 60000}m"
    }

    if (showPreview != null) {
        WallpaperPreviewScreen(
            stateName = showPreview!!,
            onClose = { showPreview = null },
            onApply = {
                scope.launch {
                    try {
                        val wm = WallpaperManager.getInstance(context)
                        val bmp = generateWallpaperBitmap(showPreview!!)
                        wm.setBitmap(bmp)
                        Toast.makeText(context, "Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                        showPreview = null
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wellness Wallpaper") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("BACK") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Auto Mode", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Wallpaper changes dynamically based on your usage.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Switch(
                        checked = autoMode,
                        onCheckedChange = { 
                            autoMode = it
                            scope.launch { repo.saveSetting("wallpaper_auto_mode", it.toString()) }
                        }
                    )
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wellness Insights", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(insightMessage, fontSize = 14.sp)
                    if (autoMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            showPreview = currentWellnessState.name
                        }) {
                            Text("Preview Auto Wallpaper")
                        }
                    }
                }
            }

            if (!autoMode) {
                Text("Select Manual Wallpaper", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WellnessState.values().filter { it != WellnessState.Unknown }) { state ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPreview = state.name }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(state.name, fontSize = 16.sp)
                                if (manualCategory == state.name) {
                                    Text("Active", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperPreviewScreen(stateName: String, onClose: () -> Unit, onApply: () -> Unit) {
    val bitmap = remember(stateName) { generateWallpaperBitmap(stateName) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Preview: $stateName", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onClose) { Text("Cancel") }
                Button(onClick = onApply) { Text("Apply Wallpaper") }
            }
        }
    }
}

// Generates an attractive synthetic wallpaper (gradients, colors) depending on current wellness
fun generateWallpaperBitmap(state: String): Bitmap {
    val bmp = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint().apply { isAntiAlias = true }
    
    val baseColor = when(state) {
        "Focused" -> Color.parseColor("#1b5e20") // Deep Green
        "Balanced" -> Color.parseColor("#0d47a1") // Deep Blue
        "Mindful" -> Color.parseColor("#b0bec5") // Calm Gray/Blue
        "Distracted" -> Color.parseColor("#e65100") // Orange
        "Overloaded" -> Color.parseColor("#b71c1c") // Red
        else -> Color.DKGRAY
    }
    
    canvas.drawColor(baseColor)
    
    // Add some simple abstract shapes
    paint.color = Color.WHITE
    paint.alpha = 30
    canvas.drawCircle(300f, 400f, 600f, paint)
    canvas.drawCircle(800f, 1500f, 400f, paint)
    
    return bmp
}
