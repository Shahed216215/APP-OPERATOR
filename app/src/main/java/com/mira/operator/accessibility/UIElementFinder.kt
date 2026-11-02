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

    /**
     * Finds visible UI elements only inside an authorized app.
     *
     * Password and editable-field contents are never collected.
     * Results remain in memory and are not logged or persisted.
     */
    fun findElements(
        root: AccessibilityNodeInfo?,
        maxNodes: Int = MAX_NODES
    ): List<UIElementSnapshot> {

        if (root == null) return emptyList()

        val packageName = root.packageName?.toString() ?: return emptyList()

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

                if (depth > MAX_DEPTH) continue

                val isPassword = node.isPassword
                val isEditable = node.isEditable

                val safeText = if (!isPassword && !isEditable) {
                    node.text
                        ?.toString()
                        ?.takeIf { it.isNotBlank() }
                        ?.take(2000)
                } else {
                    null
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
            }
        } finally {
            while (stack.isNotEmpty()) {
                stack.removeLast().first.recycle()
            }
        }

        return results
    }

    fun findByText(
        elements: List<UIElementSnapshot>,
        query: String
    ): List<UIElementSnapshot> {

        if (query.isBlank()) return emptyList()

        return elements.filter { element ->
            element.text?.contains(query, ignoreCase = true) == true
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
