@file:Suppress("DEPRECATION")
package com.nullify.cleaner.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class CleanerAccessibilityService : AccessibilityService() {

    companion object {
        var isRunning = false
            private set
        var targetPackage: String? = null
            private set
        var actionCallback: ((Boolean) -> Unit)? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName == "com.android.settings") {
                findAndClickClearCache()
            }
        }
    }

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    fun navigateToAppSettings(packageName: String) {
        targetPackage = packageName
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun findAndClickClearCache(): Boolean {
        val root = rootInActiveWindow ?: return false
        val cacheButtons = root.findAccessibilityNodeInfosByText("Clear cache")
        if (cacheButtons.isNotEmpty()) {
            cacheButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            actionCallback?.invoke(true)
            targetPackage = null
            return true
        }

        val storageSection = findStorageSection(root)
        if (storageSection != null) {
            storageSection.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            root.recycle() // DEPRECATED
            return findAndClickClearCache()
        }

        root.recycle() // DEPRECATED
        actionCallback?.invoke(false)
        targetPackage = null
        return false
    }

    private fun findStorageSection(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains("Storage", ignoreCase = true) == true) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findStorageSection(child)
            if (result != null) return result
            child.recycle()
        }
        return null
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 200) {
        val path = Path().apply { moveTo(startX, startY); lineTo(endX, endY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
