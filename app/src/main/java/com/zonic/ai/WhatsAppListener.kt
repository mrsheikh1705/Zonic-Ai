package com.zonic.ai

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class WhatsAppListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp" && sbn.packageName != "com.whatsapp.w4b") return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val e = n.extras
        val who = e.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val msg = e.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        Speaker.say(applicationContext, "$who ka WhatsApp message aaya hai. Unhon ne likha: $msg")
        Bus.onMsg?.invoke(who, msg)
    }
}
