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
import com.mira.operator.security.AppConsentManager
import com.mira.operator.security.EmergencyStopController

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
            text = if (EmergencyStopController.isEmergencyStopEnabled(this)) {
                "Emergency Stop is ON.\nAutomation engine is not active."
            } else {
                "Safety gate is READY.\nAutomation engine is not active."
            }
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

        val chatGptConsentButton =
            createConsentButton("com.openai.chatgpt", "ChatGPT")

        val geminiConsentButton =
            createConsentButton(
                "com.google.android.googlequicksearchbox",
                "Gemini"
            )

        val chromeConsentButton =
            createConsentButton("com.android.chrome", "Google Chrome")

        val stopButton = Button(this).apply {
            text = "Emergency Stop"
            setOnClickListener {
                val saved = EmergencyStopController.activate(this@MainActivity)

                statusText.text = if (saved) {
                    "Emergency Stop is ON."
                } else {
                    "Emergency Stop is ON in memory. Storage verification failed."
                }

                Toast.makeText(
                    this@MainActivity,
                    if (saved) "Emergency Stop activated"
                    else "Stop enforced in memory; persistence failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val resetButton = Button(this).apply {
            text = "Reset Emergency Stop"
            setOnClickListener {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("Reset Emergency Stop?")
                    .setMessage(
                        "This will enable the safety gate. " +
                        "It will not start automation."
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Confirm Reset") { _, _ ->
                        val reset = EmergencyStopController
                            .resetAfterUserConfirmation(
                                this@MainActivity,
                                userConfirmed = true
                            )

                        statusText.text = if (reset) {
                            "Safety gate is READY. Automation engine is not active."
                        } else {
                            "Emergency Stop remains ON. Reset could not be verified."
                        }

                        Toast.makeText(
                            this@MainActivity,
                            if (reset) "Safety gate reset"
                            else "Reset failed; execution remains blocked",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .show()
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(accessibilityButton)
        layout.addView(chatGptButton)
        layout.addView(geminiButton)
        layout.addView(chatGptConsentButton)
        layout.addView(geminiConsentButton)
        layout.addView(chromeConsentButton)
        layout.addView(stopButton)
        layout.addView(resetButton)

        setContentView(layout)
    }

    private fun createConsentButton(
        packageName: String,
        displayName: String
    ): Button {
        return Button(this).apply {
            text = "Manage $displayName Consent"
            setOnClickListener {
                if (!AppRegistry.isAllowed(packageName)) {
                    Toast.makeText(
                        this@MainActivity,
                        "App is not developer-allowlisted",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val currentlyAuthorized =
                    AppConsentManager.isAuthorized(this@MainActivity, packageName)

                val action = if (currentlyAuthorized) "Revoke" else "Grant"

                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("$action $displayName access?")
                    .setMessage(
                        if (currentlyAuthorized) {
                            "Mira will stop inspecting this app after consent is revoked."
                        } else {
                            "Mira may observe this app's visible UI. " +
                            "This does not enable clicking, typing, or transactions."
                        }
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(action) { _, _ ->
                        val saved = if (currentlyAuthorized) {
                            AppConsentManager.revoke(this@MainActivity, packageName)
                        } else {
                            AppConsentManager.grant(this@MainActivity, packageName)
                        }

                        Toast.makeText(
                            this@MainActivity,
                            when {
                                !saved -> "Consent change could not be verified; check permission status"
                                currentlyAuthorized -> "$displayName consent revoked"
                                else -> "$displayName consent granted"
                            },
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .show()
            }
        }
    }

    private fun openAllowedApp(packageName: String) {
        if (!AppRegistry.isAllowed(packageName)) {
            Toast.makeText(this, "App is not allowlisted", Toast.LENGTH_SHORT).show()
            return
        }

        if (!AppConsentManager.isAuthorized(this, packageName)) {
            Toast.makeText(
                this,
                "Grant this app's consent before opening it through Operator",
                Toast.LENGTH_LONG
            ).show()
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
