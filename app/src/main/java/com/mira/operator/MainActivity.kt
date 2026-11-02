
package com.mira.operator

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
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

    companion object {
        private const val REQUEST_SELECT_APK = 4101
        private const val APK_MIME_TYPE =
            "application/vnd.android.package-archive"
    }

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

        val installApkButton = Button(this).apply {
            text = "Install APK (Downloads / Google Drive)"
            setOnClickListener {
                beginApkSelection()
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
        layout.addView(installApkButton)
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

    @Deprecated("Retained for compatibility with current Activity structure.")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_SELECT_APK) return

        if (resultCode != RESULT_OK) {
            showMessage("APK selection cancelled.")
            return
        }

        val apkUri = data?.data

        if (apkUri == null) {
            showMessage("No APK file was selected.")
            return
        }

        if (apkUri.scheme != "content") {
            showMessage("Unsupported file source. Use the system file picker.")
            return
        }

        launchSystemPackageInstaller(apkUri)
    }

    private fun beginApkSelection() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()
        ) {
            try {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
                startActivity(settingsIntent)
                showMessage(
                    "Allow APK installation, return, and tap Install APK again."
                )
            } catch (e: ActivityNotFoundException) {
                showMessage("Unknown-app install settings are unavailable.")
            } catch (e: Exception) {
                showMessage("Unable to open APK permission settings.")
            }
            return
        }

        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = APK_MIME_TYPE
        }

        try {
            startActivityForResult(
                pickerIntent,
                REQUEST_SELECT_APK
            )
        } catch (e: ActivityNotFoundException) {
            showMessage("No compatible file picker is available.")
        } catch (e: Exception) {
            showMessage("Unable to open the file picker.")
        }
    }

    private fun launchSystemPackageInstaller(apkUri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(
                contentResolver,
                "Selected APK",
                apkUri
            )
        }

        try {
            startActivity(installIntent)
        } catch (e: ActivityNotFoundException) {
            showMessage("No Android package installer is available.")
        } catch (e: SecurityException) {
            showMessage(
                "Installer cannot access this file. Download it to Downloads and retry."
            )
        } catch (e: Exception) {
            showMessage(
                "Could not open installer. Try downloading the APK to Downloads."
            )
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
            Toast.LENGTH_LONG
        ).show()
    }
}
