package com.mira.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mira.operator.OperatorAuditLog
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

        @Volatile
        var instance: OperatorAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isConnected = true
        OperatorAuditLog.record(
            applicationContext,
            "ACCESSIBILITY_CONNECTED",
            packageName,
            "CONNECTED"
        )
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isConnected) return

        // Fail closed while Emergency Stop is active (default state is ON).
        val stopped = getSharedPreferences("operator", MODE_PRIVATE)
            .getBoolean("emergency_stop", true)
        if (stopped) {
            activePackageName = null
            return
        }

        val eventPackage = event.packageName?.toString() ?: return
        if (!AppRegistry.isAllowed(eventPackage)) {
            activePackageName = null
            return
        }

        activePackageName = eventPackage

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> inspectActiveWindow()
        }
    }

    private fun inspectActiveWindow() {
        val root: AccessibilityNodeInfo = rootInActiveWindow ?: return
        try {
            val rootPackage = root.packageName?.toString() ?: return
            if (!AppRegistry.isAllowed(rootPackage)) return

            // Observation only. No UI text is logged or persisted.
            // Action execution is deliberately not enabled in this milestone.
            Log.d(TAG, "Authorized app window observed")
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {
        activePackageName = null
        OperatorAuditLog.record(
            applicationContext,
            "ACCESSIBILITY_INTERRUPTED",
            null,
            "INTERRUPTED"
        )
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        isConnected = false
        activePackageName = null
        instance = null
        OperatorAuditLog.record(
            applicationContext,
            "ACCESSIBILITY_DISCONNECTED",
            null,
            "DISCONNECTED"
        )
        Log.i(TAG, "Accessibility service disconnected")
        super.onDestroy()
    }
}
