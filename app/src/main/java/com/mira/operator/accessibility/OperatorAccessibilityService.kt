package com.mira.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.mira.operator.security.AppConsentManager
import com.mira.operator.security.EmergencyStopController

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
        if (EmergencyStopController.isEmergencyStopEnabled(this)) {
            activePackageName = null
            return
        }

        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Only inspect user-authorized applications.
        if (!AppConsentManager.isAuthorized(this, packageName)) {
            activePackageName = null
            return
        }

        activePackageName = packageName

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                inspectActiveWindow(packageName)
            }
        }
    }

    private fun inspectActiveWindow(expectedPackageName: String) {
        if (!isConnected ||
            EmergencyStopController.isEmergencyStopEnabled(this) ||
            !AppConsentManager.isAuthorized(this, expectedPackageName)
        ) {
            activePackageName = null
            return
        }

        val root: AccessibilityNodeInfo =
            rootInActiveWindow ?: return

        try {
            if (EmergencyStopController.isEmergencyStopEnabled(this) ||
                !AppConsentManager.isAuthorized(this, expectedPackageName)
            ) {
                activePackageName = null
                return
            }

            val packageName = root.packageName?.toString() ?: return

            if (packageName != expectedPackageName ||
                !AppConsentManager.isAuthorized(this, packageName)
            ) {
                activePackageName = null
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
        isConnected = false
        activePackageName = null
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        isConnected = false
        activePackageName = null

        Log.i(TAG, "Accessibility service disconnected")

        super.onDestroy()
    }
}
