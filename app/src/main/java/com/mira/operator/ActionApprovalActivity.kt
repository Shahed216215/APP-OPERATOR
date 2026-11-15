package com.mira.operator

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.mira.operator.accessibility.OperatorAccessibilityService
import com.mira.operator.apps.AppRegistry

/**
 * Internal, non-exported action approval surface.
 * A bridge request must open this screen; it cannot call the service executor directly.
 */
class ActionApprovalActivity : Activity() {

    private var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.getStringExtra(EXTRA_ACTION).orEmpty()
        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val target = intent.getStringExtra(EXTRA_TARGET).orEmpty()
        val value = intent.getStringExtra(EXTRA_VALUE).orEmpty()
        val contextLabel = intent.getStringExtra(EXTRA_CONTEXT).orEmpty()
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID).orEmpty()

        if (!isValidRequest(action, targetPackage, requestId)) {
            finishWithReceipt(action, targetPackage, "REJECTED_INVALID_REQUEST")
            return
        }

        val title = TextView(this).apply {
            text = "APPROVAL REQUIRED"
            textSize = 21f
            gravity = Gravity.CENTER_VERTICAL
        }

        val preview = TextView(this).apply {
            textSize = 16f
            setPadding(0, 24, 0, 24)
            text = buildString {
                append("App: ")
                append(AppRegistry.getDisplayName(targetPackage) ?: targetPackage)
                append("\nPackage: ")
                append(targetPackage)
                append("\nAction: ")
                append(actionDescription(action))
                if (target.isNotBlank()) {
                    append("\nTarget: ")
                    append(target)
                }
                if (contextLabel.isNotBlank()) {
                    append("\nExpected screen/conversation: ")
                    append(contextLabel)
                }
                if (action == "SET_TEXT" || action == "SUBMIT_SEARCH" || action == "SEND_CHAT") {
                    append("\nExact text/content:\n")
                    append(value)
                }
                append("\n\nThis approval applies to this one action only.")
                append("\nThe next action will require a new approval.")
            }
        }

        val approve = Button(this).apply {
            text = "Approve this action"
            setOnClickListener {
                AlertDialog.Builder(this@ActionApprovalActivity)
                    .setTitle("Confirm this exact action")
                    .setMessage("Run the action shown above once? No later action is approved.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Approve once") { _, _ ->
                        val service = OperatorAccessibilityService.instance
                        val result = service?.executeApprovedAction(
                            action, targetPackage, target.ifBlank { null },
                            value.takeIf { action in setOf("SET_TEXT", "SUBMIT_SEARCH", "SEND_CHAT") },
                            contextLabel.ifBlank { null }
                        ) ?: "INCONCLUSIVE_SERVICE_DISCONNECTED"
                        finishWithReceipt(action, targetPackage, result)
                    }
                    .show()
            }
        }

        val deny = Button(this).apply {
            text = "Deny / Cancel"
            setOnClickListener {
                finishWithReceipt(action, targetPackage, "REJECTED_BY_USER")
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 36, 32, 32)
            addView(title)
            addView(preview)
            addView(approve)
            addView(deny)
        }
        setContentView(layout)
    }

    private fun isValidRequest(action: String, targetPackage: String, requestId: String): Boolean {
        val supported = setOf("INSPECT_UI", "TAP_TEXT", "SET_TEXT", "SUBMIT_SEARCH", "SEND_CHAT", "BACK", "SWIPE_UP", "SWIPE_DOWN")
        if (action !in supported || !AppRegistry.isAllowed(targetPackage)) return false
        if (requestId.isBlank() || requestId.length > 96) return false
        if (action in setOf("SUBMIT_SEARCH", "SEND_CHAT") &&
            (intent.getStringExtra(EXTRA_TARGET).isNullOrBlank() ||
             intent.getStringExtra(EXTRA_VALUE).isNullOrBlank() ||
             intent.getStringExtra(EXTRA_CONTEXT).isNullOrBlank())
        ) return false
        if (OperatorAccessibilityService.activePackageName != targetPackage) return false
        if (getSharedPreferences("operator", MODE_PRIVATE).getBoolean("emergency_stop", true)) return false
        return true
    }

    private fun actionDescription(action: String): String = when (action) {
        "INSPECT_UI" -> "Read visible interface structure (no passwords or editable contents)"
        "TAP_TEXT" -> "Tap the unique visible element named above"
        "SET_TEXT" -> "Write the exact text above into one non-password field"
        "SUBMIT_SEARCH" -> "Submit the exact search query shown above"
        "SEND_CHAT" -> "Send the exact message shown above to the named conversation"
        "BACK" -> "Android Back navigation"
        "SWIPE_UP" -> "Swipe up once"
        "SWIPE_DOWN" -> "Swipe down once"
        else -> "Unsupported"
    }

    private fun finishWithReceipt(action: String, targetPackage: String, result: String) {
        if (completed) return
        completed = true
        OperatorAuditLog.record(this, action, targetPackage, result.substringBefore(':').take(48))
        val message = TextView(this).apply {
            text = "Result: $result\n\nNo further action was approved."
            textSize = 17f
            setPadding(32, 48, 32, 32)
        }
        setContentView(message)
        message.setOnClickListener { finish() }
        message.postDelayed({ if (!isFinishing) finish() }, 2200)
    }

    companion object {
        const val EXTRA_ACTION = "operator.action"
        const val EXTRA_PACKAGE = "operator.package"
        const val EXTRA_TARGET = "operator.target"
        const val EXTRA_VALUE = "operator.value"
        const val EXTRA_CONTEXT = "operator.context"
        const val EXTRA_REQUEST_ID = "operator.request_id"
    }
}
