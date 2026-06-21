package com.example.presentation.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.LauncherApplication
import com.example.domain.model.LifeValueLog
import kotlinx.coroutines.launch

class LifeValueReflectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra("package_name") ?: "Unknown App"
        val durationMs = intent.getLongExtra("duration_ms", 0L)
        val durationMins = durationMs / 60000L

        val appName = try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }

        setContent {
            val scope = rememberCoroutineScope()
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                ) {
                    LifeValueReflectionScreen(
                        appName = appName,
                        packageName = packageName,
                        durationMins = durationMins,
                        onResponse = { category, response ->
                            val repo = (application as LauncherApplication).container.repository
                            val log = LifeValueLog(
                                packageName = packageName,
                                appName = appName,
                                timestamp = System.currentTimeMillis(),
                                durationMs = durationMs,
                                valueCategory = category,
                                response = response
                            )
                            scope.launch {
                                repo.saveLifeValueLog(log)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LifeValueReflectionScreen(
    appName: String,
    packageName: String,
    durationMins: Long,
    onResponse: (String, String) -> Unit
) {
    var response by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Life Value Reflection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You just used $appName for $durationMins minutes.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Has this time added value to your life?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = response,
            onValueChange = { response = it },
            label = { Text("What did you accomplish or learn?") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onResponse("High Value", response) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Absolutely (High Value)", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onResponse("Medium Value", response) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Somewhat (Medium Value)", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = { onResponse("Low Value", response) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Not really (Low Value)", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}
