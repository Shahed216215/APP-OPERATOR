package com.mira.operator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mira.operator.apps.AppRegistry

class MainActivity : Activity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Mira Universal App Operator"
            textSize = 22f
        }

        statusText = TextView(this).apply {
            text = "Operator initialized.\nAutomation engine is not active."
            textSize = 15f
            setPadding(0, 24, 0, 24)
        }

        val accessibilityButton = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val chatGptButton = Button(this).apply {
            text = "Open ChatGPT"
            setOnClickListener {
                openAllowedApp("com.openai.chatgpt")
            }
        }

        val geminiButton = Button(this).apply {
            text = "Open Gemini"
            setOnClickListener {
                openAllowedApp("com.google.android.googlequicksearchbox")
            }
        }

        val stopButton = Button(this).apply {
            text = "Emergency Stop"
            setOnClickListener {
                getSharedPreferences("operator", MODE_PRIVATE)
                    .edit()
                    .putBoolean("emergency_stop", true)
                    .apply()

                statusText.text = "Emergency Stop is ON."
                Toast.makeText(
                    this@MainActivity,
                    "Automation stopped",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(accessibilityButton)
        layout.addView(chatGptButton)
        layout.addView(geminiButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun openAllowedApp(packageName: String) {
        if (!AppRegistry.isAllowed(packageName)) {
            Toast.makeText(this, "App is not allowlisted", Toast.LENGTH_SHORT).show()
            return
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent == null) {
            Toast.makeText(this, "App is not installed or unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            startActivity(launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
        }
    }
}
