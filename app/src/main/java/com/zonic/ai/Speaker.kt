package com.zonic.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val pending = ArrayList<String>()

    fun say(ctx: Context, text: String) {
        if (tts == null) {
            tts = TextToSpeech(ctx.applicationContext) { st ->
                val t = tts
                if (st == TextToSpeech.SUCCESS && t != null) {
                    val r = t.setLanguage(Locale("ur", "PK"))
                    if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                        t.setLanguage(Locale("hi", "IN"))
                    }
                    ready = true
                    for (p in pending) t.speak(p, TextToSpeech.QUEUE_ADD, null, null)
                    pending.clear()
                }
            }
        }
        val t = tts
        if (ready && t != null) t.speak(text, TextToSpeech.QUEUE_ADD, null, null) else pending.add(text)
    }

    fun stop() {
        pending.clear()
        tts?.stop()
    }
}
