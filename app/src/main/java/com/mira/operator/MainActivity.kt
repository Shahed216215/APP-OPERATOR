
package com.mira.operator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mira.operator.accessibility.OperatorSafetyGate
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
            textSize = 15f
            setPadding(0, 24, 0, 24)
        }

        val accessibilityButton = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        val chatGptButton = Button(this).apply {
            text = "Open ChatGPT"
            setOnClickListener {
                if (OperatorSafetyGate.isStopped(this@MainActivity)) {
                    showMessage("Resume the Operator before using it.")
                    return@setOnClickListener
                }

                openAllowedApp("com.openai.chatgpt")
            }
        }

        val stopButton = Button(this).apply {
            text = "Emergency Stop"
            setOnClickListener {
                val saved = OperatorSafetyGate.stop(this@MainActivity)

                refreshStatus()

                if (saved) {
                    showMessage("Emergency Stop is ON.")
                } else {
                    showMessage("Unable to save Stop state.")
                }
            }
        }

        val resumeButton = Button(this).apply {
            text = "Resume Operator"
            setOnClickListener {
                val saved = OperatorSafetyGate.resume(this@MainActivity)

                refreshStatus()

                if (saved) {
                    showMessage("Operator resumed by user.")
                } else {
                    showMessage("Resume failed. Operator remains stopped.")
                }
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(accessibilityButton)
        layout.addView(chatGptButton)
        layout.addView(stopButton)
        layout.addView(resumeButton)

        setContentView(layout)
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()

        if (::statusText.isInitialized) {
            refreshStatus()
        }
    }

    private fun refreshStatus() {
        statusText.text =
            if (OperatorSafetyGate.isStopped(this)) {
                "Status: STOPPED\nAutomation is disabled."
            } else {
                "Status: RESUMED\nUser authorization is active."
            }
    }

    private fun openAllowedApp(packageName: String) {
        if (OperatorSafetyGate.isStopped(this)) {
            showMessage("Operator is stopped.")
            return
        }

        if (!AppRegistry.isAllowed(packageName)) {
            showMessage("App is not allowlisted.")
            return
        }

        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent == null) {
            showMessage("ChatGPT is not installed or unavailable.")
            return
        }

        try {
            startActivity(launchIntent)
        } catch (e: Exception) {
            showMessage("Unable to open ChatGPT.")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
