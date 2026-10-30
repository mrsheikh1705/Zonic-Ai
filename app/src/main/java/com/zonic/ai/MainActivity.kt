package com.zonic.ai

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var pendingMic = false
    private var fileMime: String? = null
    private var fileB64: String? = null

    private fun js(code: String) {
        main.post { web.evaluateJavascript(code, null) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.setBackgroundColor(Color.parseColor("#070b14"))
        web.addJavascriptInterface(Bridge(), "Android")
        web.loadUrl("file:///android_asset/index.html")
        setContentView(web)
    }

    override fun onResume() {
        super.onResume()
        Bus.onMsg = { who, msg ->
            js("window.onWhatsApp(" + JSONObject.quote(who) + "," + JSONObject.quote(msg) + ")")
        }
    }

    override fun onPause() {
        super.onPause()
        Bus.onMsg = null
    }

    private fun startRec() {
        main.post {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
                try {
                    startActivityForResult(i, 1)
                } catch (e: Exception) {
                    js("window.onVoiceErr('Is phone par voice input available nahi')")
                }
                return@post
            }
            recognizer?.destroy()
            val r = SpeechRecognizer.createSpeechRecognizer(this)
            r.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(t: Int, p: Bundle?) {}
                override fun onPartialResults(p: Bundle?) {}
                override fun onError(code: Int) {
                    js("window.onVoiceErr('Awaaz samajh nahi aayi, dobara bolein (code $code)')")
                }
                override fun onResults(res: Bundle?) {
                    val t = res?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (t != null) js("window.onVoice(" + JSONObject.quote(t) + ")")
                    else js("window.onVoiceErr('Kuch sunai nahi diya')")
                }
            })
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            r.startListening(i)
            recognizer = r
        }
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, perms, res)
        if (code == 7 && pendingMic) {
            pendingMic = false
            if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) startRec()
            else js("window.onVoiceErr('Mic ki ijazat nahi mili')")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == 1) {
            val t = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (t != null) js("window.onVoice(" + JSONObject.quote(t) + ")")
        } else if (requestCode == 2) {
            val uri = data?.data ?: return
            Thread {
                try {
                    var name = "document"
                    contentResolver.query(uri, null, null, null, null)?.use {
                        if (it.moveToFirst()) {
                            val ix = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (ix >= 0) name = it.getString(ix)
                        }
                    }
                    val bytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                    if (bytes.size > 8_000_000) {
                        js("window.onVoiceErr('File bohat badi hai (8MB se kam rakhein)')")
                    } else {
                        fileMime = contentResolver.getType(uri) ?: "application/octet-stream"
                        fileB64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        js("window.onFile(" + JSONObject.quote(name) + ")")
                    }
                } catch (e: Exception) {
                    js("window.onVoiceErr('File nahi khul saki')")
                }
            }.start()
        }
    }

    inner class Bridge {
        @JavascriptInterface
        fun getKey(): String = getSharedPreferences("z", MODE_PRIVATE).getString("key", "") ?: ""

        @JavascriptInterface
        fun setKey(k: String) {
            getSharedPreferences("z", MODE_PRIVATE).edit().putString("key", k.trim()).apply()
        }

        @JavascriptInterface
        fun speak(t: String) {
            Speaker.say(this@MainActivity, t)
        }

        @JavascriptInterface
        fun stopSpeak() {
            Speaker.stop()
        }

        @JavascriptInterface
        fun listen() {
            main.post {
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    pendingMic = true
                    requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 7)
                } else {
                    startRec()
                }
            }
        }

        @JavascriptInterface
        fun notifSettings() {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        @JavascriptInterface
        fun pickFile() {
            main.post {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*", "text/*"))
                startActivityForResult(i, 2)
            }
        }

        @JavascriptInterface
        fun clearFile() {
            fileMime = null
            fileB64 = null
        }

        @JavascriptInterface
        fun accessOn(): Boolean = ZonicAccess.inst != null

        @JavascriptInterface
        fun openAccess() {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        @JavascriptInterface
        fun cancelTask() {
            PhoneAgent.cancel = true
        }

        @JavascriptInterface
        fun runTask(id: Int, goal: String) {
            val key = getKey()
            Thread {
                val out = if (key.isEmpty()) "ERR: Pehle Settings mein Gemini key save karein"
                else PhoneAgent.run(key, goal) { st -> js("window.onStep(" + JSONObject.quote(st) + ")") }
                try {
                    val i = Intent(this@MainActivity, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(i)
                } catch (e: Exception) {
                }
                js("window.onTaskDone($id," + JSONObject.quote(out) + ")")
            }.start()
        }

        @JavascriptInterface
        fun gemini(id: Int, prompt: String, useFile: Boolean) {
            val key = getKey()
            val m = if (useFile) fileMime else null
            val f = if (useFile) fileB64 else null
            Thread {
                val out = if (key.isEmpty()) "ERR: Pehle Settings mein Gemini key save karein" else Gemini.ask(key, prompt, m, f)
                js("window.onGemini($id," + JSONObject.quote(out) + ")")
            }.start()
        }
    }
}
