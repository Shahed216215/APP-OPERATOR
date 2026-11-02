
package com.mira.operator.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.mira.operator.apps.AppRegistry

data class UIElementSnapshot(
    val text: String?,
    val className: String?,
    val viewId: String?,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isEnabled: Boolean,
    val bounds: Rect,
    val depth: Int
)

object UIElementFinder {

    private const val MAX_NODES = 500
    private const val MAX_DEPTH = 30
    private const val MAX_TEXT_LENGTH = 2000

    private val sensitiveKeywordPattern = Regex(
        """(?i)\b(password|passcode|passwd|secret|otp|one.time.password|verification code|security code|cvv|cvc|card number|credit card|debit card|bank account|routing number|payment|checkout|transaction|wallet)\b|পাসওয়ার্ড|পাসওয়ার্ড|ওটিপি|এককালীন কোড|ভেরিফিকেশন কোড|নিরাপত্তা কোড|পিন|কার্ড নম্বর|কার্ড|পেমেন্ট|ব্যাংক|লেনদেন"""
    )

    private val longCardNumberPattern = Regex(
        """(?<!\d)(?:\d[ -]?){13,19}(?!\d)"""
    )

    /**
     * Conservative text filter.
     *
     * This is heuristic protection, not guaranteed detection.
     * Unknown or ambiguous sensitive formats may remain undetected.
     */
    private fun isSensitiveText(text: String): Boolean {
        return sensitiveKeywordPattern.containsMatchIn(text) ||
            longCardNumberPattern.containsMatchIn(text)
    }

    /**
     * Inspects an authorized app's visible UI.
     *
     * Security:
     * - Rejects packages outside AppRegistry.
     * - Never reads password or editable-field contents.
     * - Filters recognizable sensitive text.
     * - Does not log or persist UI text.
     * - Does not perform clicks or text entry.
     *
     * Ownership:
     * - The caller retains ownership of root.
     * - Child nodes obtained with getChild() are recycled here.
     */
    fun findElements(
        root: AccessibilityNodeInfo?,
        maxNodes: Int = MAX_NODES
    ): List<UIElementSnapshot> {

        if (root == null) return emptyList()

        val packageName = root.packageName?.toString()
            ?: return emptyList()

        if (!AppRegistry.isAllowed(packageName)) {
            return emptyList()
        }

        val safeLimit = maxNodes.coerceIn(1, MAX_NODES)
        val results = mutableListOf<UIElementSnapshot>()

        val stack = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        stack.addLast(root to 0)

        try {
            while (stack.isNotEmpty() && results.size < safeLimit) {

                val (node, depth) = stack.removeLast()
                val isRoot = node === root

                try {
                    if (depth > MAX_DEPTH) continue

                    val nodePackage = node.packageName?.toString()
                    if (nodePackage != packageName) continue

                    val isPassword = node.isPassword
                    val isEditable = node.isEditable

                    val candidateText = if (!isPassword && !isEditable) {
                        node.text
                            ?.toString()
                            ?.takeIf { it.isNotBlank() }
                            ?.take(MAX_TEXT_LENGTH)
                    } else {
                        null
                    }

                    val safeText = candidateText?.takeUnless {
                        isSensitiveText(it)
                    }

                    val bounds = Rect()
                    node.getBoundsInScreen(bounds)

                    results.add(
                        UIElementSnapshot(
                            text = safeText,
                            className = node.className?.toString(),
                            viewId = node.viewIdResourceName,
                            isClickable = node.isClickable,
                            isEditable = isEditable,
                            isEnabled = node.isEnabled,
                            bounds = Rect(bounds),
                            depth = depth
                        )
                    )

                    if (depth < MAX_DEPTH) {
                        for (index in 0 until node.childCount) {

                            if (results.size + stack.size >= safeLimit) {
                                break
                            }

                            val child = node.getChild(index) ?: continue
                            stack.addLast(child to depth + 1)
                        }
                    }

                } finally {
                    if (!isRoot) {
                        node.recycle()
                    }
                }
            }

        } finally {
            while (stack.isNotEmpty()) {
                val pendingNode = stack.removeLast().first

                if (pendingNode !== root) {
                    pendingNode.recycle()
                }
            }
        }

        return results
    }

    fun findByText(
        elements: List<UIElementSnapshot>,
        query: String
    ): List<UIElementSnapshot> {

        if (query.isBlank() || isSensitiveText(query)) {
            return emptyList()
        }

        return elements.filter { element ->
            element.text?.contains(
                query,
                ignoreCase = true
            ) == true
        }
    }

    fun findClickableElements(
        elements: List<UIElementSnapshot>
    ): List<UIElementSnapshot> {

        return elements.filter {
            it.isClickable && it.isEnabled
        }
    }

    fun findEditableElements(
        elements: List<UIElementSnapshot>
    ): List<UIElementSnapshot> {

        return elements.filter {
            it.isEditable && it.isEnabled
        }
    }
}
