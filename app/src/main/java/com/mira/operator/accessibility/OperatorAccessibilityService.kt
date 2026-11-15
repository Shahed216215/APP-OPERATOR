package com.mira.operator.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mira.operator.OperatorAuditLog
import com.mira.operator.apps.AppRegistry

class OperatorAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "MiraOperatorService"
        @Volatile var isConnected: Boolean = false
            private set
        @Volatile var activePackageName: String? = null
            private set
        @Volatile var instance: OperatorAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isConnected = true
        OperatorAuditLog.record(applicationContext, "ACCESSIBILITY_CONNECTED", packageName, "CONNECTED")
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isConnected) return
        if (isEmergencyStopped()) {
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
        val root = rootInActiveWindow ?: return
        try {
            val rootPackage = root.packageName?.toString() ?: return
            if (!AppRegistry.isAllowed(rootPackage)) return
            Log.d(TAG, "Authorized app window observed")
        } finally {
            root.recycle()
        }
    }

    /** Must only be called after the user approves the exact preview. */
    fun executeApprovedAction(
        actionType: String,
        expectedPackage: String,
        target: String?,
        value: String?,
        expectedContext: String?
    ): String {
        if (!isConnected) return "INCONCLUSIVE_SERVICE_DISCONNECTED"
        if (isEmergencyStopped()) return "STOPPED_EMERGENCY_STOP"
        if (!AppRegistry.isAllowed(expectedPackage)) return "REJECTED_PACKAGE_NOT_ALLOWED"
        if (activePackageName != expectedPackage) return "REJECTED_TARGET_APP_CHANGED"

        val root = rootInActiveWindow ?: return "INCONCLUSIVE_NO_ACTIVE_WINDOW"
        try {
            if (root.packageName?.toString() != expectedPackage) return "REJECTED_WINDOW_PACKAGE_MISMATCH"
            if (!expectedContext.isNullOrBlank() &&
                !containsVisibleLabel(root, expectedContext, expectedPackage)
            ) return "REJECTED_CONTEXT_NOT_FOUND"

            val result = when (actionType) {
                "BACK" -> if (performGlobalAction(GLOBAL_ACTION_BACK)) "DISPATCHED" else "FAILED_ACTION_REJECTED"
                "SWIPE_UP" -> dispatchVerticalSwipe(up = true)
                "SWIPE_DOWN" -> dispatchVerticalSwipe(up = false)
                "TAP_TEXT" -> if (target.isNullOrBlank() || target.length > 128) {
                    "REJECTED_INVALID_TARGET"
                } else clickUniqueText(root, target, expectedPackage)
                "SET_TEXT" -> if (target.isNullOrBlank() || target.length > 200 ||
                    value == null || value.length > 2000
                ) {
                    "REJECTED_INVALID_TEXT_REQUEST"
                } else setTextByViewId(root, target, value, expectedPackage)
                "INSPECT_UI" -> {
                    val snapshot = UIElementFinder.findElements(root, 200)
                    "OBSERVED_${snapshot.size}_NODES"
                }
                else -> "REJECTED_UNKNOWN_ACTION"
            }
            return result
        } finally {
            root.recycle()
        }
    }

    private fun isEmergencyStopped(): Boolean =
        getSharedPreferences("operator", MODE_PRIVATE).getBoolean("emergency_stop", true)

    private fun dispatchVerticalSwipe(up: Boolean): String {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        if (width <= 0f || height <= 0f) return "INCONCLUSIVE_INVALID_SCREEN_SIZE"
        val x = width * 0.5f
        val fromY = if (up) height * 0.78f else height * 0.28f
        val toY = if (up) height * 0.28f else height * 0.78f
        val path = Path().apply { moveTo(x, fromY); lineTo(x, toY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 350))
            .build()
        return if (dispatchGesture(gesture, null, null)) "DISPATCHED" else "FAILED_GESTURE_REJECTED"
    }

    private fun containsVisibleLabel(root: AccessibilityNodeInfo, label: String, expectedPackage: String): Boolean {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var found = false
        try {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                val isRoot = node === root
                try {
                    if (node.packageName?.toString() != expectedPackage) continue
                    if (!node.isPassword && !node.isEditable &&
                        node.text?.toString()?.contains(label, ignoreCase = true) == true
                    ) { found = true; break }
                    for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
                } finally {
                    if (!isRoot) node.recycle()
                }
            }
        } finally {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                if (node !== root) node.recycle()
            }
        }
        return found
    }

    private fun clickUniqueText(root: AccessibilityNodeInfo, target: String, expectedPackage: String): String {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var match: AccessibilityNodeInfo? = null
        var count = 0
        try {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                val isRoot = node === root
                try {
                    if (node.packageName?.toString() != expectedPackage) continue
                    val textMatches = !node.isPassword && !node.isEditable &&
                        node.text?.toString()?.trim()?.equals(target.trim(), ignoreCase = true) == true
                    if (textMatches && node.isEnabled) {
                        val clickable = if (node.isClickable) {
                            AccessibilityNodeInfo.obtain(node)
                        } else {
                            val parent = node.parent
                            if (parent != null && parent.isClickable && parent.isEnabled) {
                                AccessibilityNodeInfo.obtain(parent).also { parent.recycle() }
                            } else {
                                parent?.recycle()
                                null
                            }
                        }
                        if (clickable != null) {
                            count++
                            if (count == 1) match = clickable else clickable.recycle()
                        }
                    }
                    for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
                } finally {
                    if (!isRoot) node.recycle()
                }
            }
        } finally {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                if (node !== root) node.recycle()
            }
        }
        val selected = match ?: return "REJECTED_TARGET_NOT_CLICKABLE"
        if (count != 1) { selected.recycle(); return "REJECTED_AMBIGUOUS_TARGET" }
        return try {
            if (selected.packageName?.toString() != expectedPackage) "REJECTED_TARGET_PACKAGE_MISMATCH"
            else if (selected.performAction(AccessibilityNodeInfo.ACTION_CLICK)) "DISPATCHED"
            else "FAILED_ACTION_REJECTED"
        } finally {
            selected.recycle()
        }
    }

    private fun setTextByViewId(
        root: AccessibilityNodeInfo,
        viewId: String,
        newText: String,
        expectedPackage: String
    ): String {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        val matches = mutableListOf<AccessibilityNodeInfo>()
        var passwordFieldPresent = false
        var authenticationScreenDetected = false
        val authMarkers = listOf("password", "passcode", "sign in", "log in", "login", "one-time code", "verification code", "otp")
        try {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                val isRoot = node === root
                try {
                    if (node.packageName?.toString() != expectedPackage) continue
                    if (node.isPassword) passwordFieldPresent = true
                    val visibleLabel = node.text?.toString()?.lowercase().orEmpty()
                    if (authMarkers.any { marker -> visibleLabel.contains(marker) }) {
                        authenticationScreenDetected = true
                    }
                    if (node.viewIdResourceName == viewId && node.isEditable && node.isEnabled && !node.isPassword) {
                        matches.add(AccessibilityNodeInfo.obtain(node))
                    }
                    for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
                } finally {
                    if (!isRoot) node.recycle()
                }
            }
        } finally {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                if (node !== root) node.recycle()
            }
        }
        if (passwordFieldPresent || authenticationScreenDetected) {
            matches.forEach { it.recycle() }
            return "REJECTED_AUTHENTICATION_SCREEN"
        }
        if (matches.isEmpty()) return "REJECTED_EDITABLE_FIELD_NOT_FOUND"
        if (matches.size != 1) {
            matches.forEach { it.recycle() }
            return "REJECTED_AMBIGUOUS_FIELD"
        }
        val node = matches.single()
        return try {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
            }
            if (node.packageName?.toString() != expectedPackage) "REJECTED_TARGET_PACKAGE_MISMATCH"
            else if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) "DISPATCHED"
            else "FAILED_ACTION_REJECTED"
        } finally {
            node.recycle()
        }
    }

    override fun onInterrupt() {
        activePackageName = null
        OperatorAuditLog.record(applicationContext, "ACCESSIBILITY_INTERRUPTED", null, "INTERRUPTED")
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        isConnected = false
        activePackageName = null
        instance = null
        OperatorAuditLog.record(applicationContext, "ACCESSIBILITY_DISCONNECTED", null, "DISCONNECTED")
        Log.i(TAG, "Accessibility service disconnected")
        super.onDestroy()
    }
}
