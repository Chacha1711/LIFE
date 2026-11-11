package com.lifequest.life

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class FocusBlockingService : AccessibilityService() {
    private var lastBlockedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        val prefs = getSharedPreferences("life_focus", Context.MODE_PRIVATE)
        if (packageName == packageNameForOwnApp()) {
            lastBlockedPackage = null
            return
        }
        if (!prefs.getBoolean("enabled", false)) return
        val blocked = prefs.getStringSet("blockedPackages", emptySet()) ?: emptySet()
        if (packageName !in blocked) {
            lastBlockedPackage = null
            return
        }
        val pauseUntil = prefs.getLong("breakUntil", 0L)
        if (System.currentTimeMillis() < pauseUntil) return
        if (lastBlockedPackage == packageName) return

        lastBlockedPackage = packageName
        val label = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (_: Exception) { "Cette application" }
        startActivity(
            Intent(this, FocusBlockActivity::class.java)
                .putExtra("blocked_app_name", label)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    private fun packageNameForOwnApp() = applicationContext.packageName

    override fun onInterrupt() {
        lastBlockedPackage = null
    }
}

class FocusBlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val blockedApp = intent.getStringExtra("blocked_app_name") ?: "Cette application"
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFFF27FA5), background = Color(0xFFFFF9F7))) {
                var message by remember { mutableStateOf("") }
                val prefs = getSharedPreferences("life_data", Context.MODE_PRIVATE)
                val coins = prefs.getInt("coins", 0)
                Surface(Modifier.fillMaxSize(), color = Color(0xFFFFF9F7)) {
                    Column(
                        Modifier.fillMaxSize().padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌿", fontSize = 54.sp)
                        Spacer(Modifier.height(18.dp))
                        Text("Petite pause !", style = MaterialTheme.typography.headlineMedium)
                        Text("$blockedApp est en pause pour le moment.", modifier = Modifier.padding(vertical = 12.dp))
                        Text("Retourne à ta mission. Chaque effort compte !", color = Color(0xFF777780))
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val focusPrefs = getSharedPreferences("life_focus", Context.MODE_PRIVATE)
                                if (coins >= 20) {
                                    prefs.edit().putInt("coins", coins - 20).apply()
                                    focusPrefs.edit().putLong("breakUntil", System.currentTimeMillis() + 15 * 60 * 1000L).apply()
                                    message = "Pause activée pendant 15 minutes."
                                    finish()
                                } else {
                                    message = "Il te faut 20 pièces pour obtenir cette pause."
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Utiliser 20 pièces · pause 15 min") }
                        OutlinedButton(
                            onClick = {
                                startActivity(Intent(this@FocusBlockActivity, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                finish()
                            }
                        ) { Text("Retourner sur LIFE") }
                        if (message.isNotBlank()) Text(message, color = Color(0xFF777780), modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
    }
}
