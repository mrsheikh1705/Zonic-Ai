package com.zonic.ai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ZonicAccess : AccessibilityService() {
    companion object {
        @Volatile
        var inst: ZonicAccess? = null
    }

    private var nodes = ArrayList<AccessibilityNodeInfo>()

    override fun onServiceConnected() {
        inst = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        inst = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun dump(): String {
        nodes = ArrayList()
        val root = rootInActiveWindow ?: return "(screen khali hai ya padhi nahi ja saki)"
        val sb = StringBuilder("App: " + root.packageName + "\n")
        walk(root, sb, 0)
        return sb.toString()
    }

    private fun walk(n: AccessibilityNodeInfo?, sb: StringBuilder, d: Int) {
        if (n == null || nodes.size >= 90 || d > 25) return
        val t = n.text?.toString()?.take(60) ?: ""
        val c = n.contentDescription?.toString()?.take(60) ?: ""
        val id = n.viewIdResourceName?.substringAfter("/") ?: ""
        val useful = t.isNotEmpty() || c.isNotEmpty() || n.isClickable || n.isEditable
        if (useful && n.isVisibleToUser) {
            val r = Rect()
            n.getBoundsInScreen(r)
            sb.append("[" + nodes.size + "] ")
            if (n.isEditable) sb.append("EDIT ") else if (n.isClickable) sb.append("BTN ")
            if (t.isNotEmpty()) sb.append("\"" + t + "\" ")
            if (c.isNotEmpty()) sb.append("desc=\"" + c + "\" ")
            if (id.isNotEmpty()) sb.append("id=" + id + " ")
            sb.append("@" + r.centerX() + "," + r.centerY() + "\n")
            nodes.add(n)
        }
        for (i in 0 until n.childCount) walk(n.getChild(i), sb, d + 1)
    }

    fun click(i: Int): Boolean {
        val n = nodes.getOrNull(i) ?: return false
        var c: AccessibilityNodeInfo? = n
        while (c != null && !c.isClickable) c = c.parent
        if (c != null && c.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        val r = Rect()
        n.getBoundsInScreen(r)
        return tap(r.centerX().toFloat(), r.centerY().toFloat())
    }

    fun tap(x: Float, y: Float): Boolean {
        val p = Path()
        p.moveTo(x, y)
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p, 0, 60))
            .build()
        return dispatchGesture(g, null, null)
    }

    fun type(i: Int, text: String): Boolean {
        val n = nodes.getOrNull(i) ?: return false
        n.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val b = Bundle()
        b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b)
    }

    private fun findScrollable(n: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (n == null) return null
        if (n.isScrollable) return n
        for (i in 0 until n.childCount) {
            val r = findScrollable(n.getChild(i))
            if (r != null) return r
        }
        return null
    }

    fun scroll(forward: Boolean): Boolean {
        val s = findScrollable(rootInActiveWindow) ?: return false
        return s.performAction(
            if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        )
    }

    fun back(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun home(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun openApp(name: String): Boolean {
        val pm = packageManager
        val q = name.trim().lowercase()
        if (q.isEmpty()) return false
        val apps = pm.getInstalledApplications(0).filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        val app = apps.firstOrNull { pm.getApplicationLabel(it).toString().lowercase() == q }
            ?: apps.firstOrNull { pm.getApplicationLabel(it).toString().lowercase().contains(q) }
            ?: return false
        val i = pm.getLaunchIntentForPackage(app.packageName) ?: return false
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        return true
    }
}
