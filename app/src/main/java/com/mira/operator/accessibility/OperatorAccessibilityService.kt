package com.mira.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.mira.operator.apps.AppRegistry

class OperatorAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "MiraOperatorService"

        @Volatile
        var isConnected: Boolean = false
            private set

        @Volatile
        var activePackageName: String? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        isConnected = true
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Emergency Stop is checked before reading package or window information.
        if (isEmergencyStopped()) {
            activePackageName = null
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Only inspect user-authorized applications.
        if (!AppRegistry.isAllowed(packageName)) {
            activePackageName = null
            return
        }

        activePackageName = packageName

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                inspectActiveWindow()
            }
        }
    }

    private fun isEmergencyStopped(): Boolean =
        getSharedPreferences("operator", MODE_PRIVATE).getBoolean("emergency_stop", false)

    private fun inspectActiveWindow() {
        // Re-check immediately before retrieving the active window.
        if (!isConnected || isEmergencyStopped()) {
            activePackageName = null
            return
        }

        val root: AccessibilityNodeInfo =
            rootInActiveWindow ?: return

        try {
            val packageName = root.packageName?.toString() ?: return

            if (!AppRegistry.isAllowed(packageName)) {
                return
            }

            // Phase 1: Observe only.
            // No clicking, typing, sending, or deleting.
            Log.d(TAG, "Authorized app window observed")

        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
        activePackageName = null
    }

    override fun onDestroy() {
        isConnected = false
        activePackageName = null

        Log.i(TAG, "Accessibility service disconnected")

        super.onDestroy()
    }
}
