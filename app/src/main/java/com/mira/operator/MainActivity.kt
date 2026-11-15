package com.mira.operator

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mira.operator.apps.AppRegistry

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun buildScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Mira Universal App Operator"
            textSize = 22f
        }

        statusText = TextView(this).apply {
            textSize = 15f
            setPadding(0, 20, 0, 20)
        }

        val accessibilityButton = Button(this).apply {
            text = "Accessibility permission settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val refreshButton = Button(this).apply {
            text = "Refresh permission status"
            setOnClickListener { refreshStatus() }
        }

        val stop = Button(this).apply {
            setOnClickListener { toggleEmergencyStop() }
        }
        stopButton = stop

        val chatGptButton = Button(this).apply {
            text = "Open ChatGPT"
            setOnClickListener { openAllowedApp("com.openai.chatgpt") }
        }

        val geminiButton = Button(this).apply {
            text = "Open Gemini"
            setOnClickListener { openAllowedApp("com.google.android.googlequicksearchbox") }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(accessibilityButton)
        layout.addView(refreshButton)
        layout.addView(stopButton)
        layout.addView(chatGptButton)
        layout.addView(geminiButton)
        setContentView(layout)
        refreshStatus()
    }

    private fun refreshStatus() {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any {
            it.resolveInfo.serviceInfo.packageName == packageName &&
                it.resolveInfo.serviceInfo.name == "com.mira.operator.accessibility.OperatorAccessibilityService"
        }

        val stopped = getSharedPreferences("operator", MODE_PRIVATE)
            .getBoolean("emergency_stop", true)

        statusText.text = buildString {
            append("Android API: ")
            append(android.os.Build.VERSION.SDK_INT)
            append("\nAccessibility permission: ")
            append(if (enabled) "ENABLED" else "NOT ENABLED")
            append("\nService connected: ")
            append(if (com.mira.operator.accessibility.OperatorAccessibilityService.isConnected) "YES" else "NO")
            append("\nEmergency Stop: ")
            append(if (stopped) "ON" else "OFF")
            append("\nMira Bridge: NOT CONNECTED")
            append("\nMode: OBSERVE ONLY — action execution is not enabled yet.")
        }

        stopButton.text = if (stopped) "Resume (manual confirmation required)" else "Emergency Stop"
    }

    private fun toggleEmergencyStop() {
        val prefs = getSharedPreferences("operator", MODE_PRIVATE)
        val stopped = prefs.getBoolean("emergency_stop", true)
        val next = !stopped

        android.app.AlertDialog.Builder(this)
            .setTitle(if (next) "Emergency Stop" else "Resume Operator")
            .setMessage(
                if (next) "Stop all queued and future automation actions?"
                else "Resume the operator? Each action will still require its own approval."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (next) "Stop now" else "Resume") { _, _ ->
                prefs.edit().putBoolean("emergency_stop", next).apply()
                OperatorAuditLog.record(
                    this,
                    action = if (next) "EMERGENCY_STOP" else "MANUAL_RESUME",
                    packageName = packageName,
                    outcome = if (next) "STOPPED" else "RESUMED"
                )
                refreshStatus()
            }
            .show()
    }

    private fun openAllowedApp(targetPackage: String) {
        if (!AppRegistry.isAllowed(targetPackage)) {
            Toast.makeText(this, "App is not allowlisted", Toast.LENGTH_SHORT).show()
            return
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (launchIntent == null) {
            Toast.makeText(this, "App is not installed or unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            startActivity(launchIntent)
            OperatorAuditLog.record(this, "OPEN_APP", targetPackage, "USER_LAUNCHED")
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
            OperatorAuditLog.record(this, "OPEN_APP", targetPackage, "FAILED")
        }
    }
}
