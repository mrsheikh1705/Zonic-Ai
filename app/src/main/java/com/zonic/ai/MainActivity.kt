package com.zonic.ai

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var logView: TextView
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private val hist = ArrayList<String>()
    private val main = Handler(Looper.getMainLooper())

    private val agents = linkedMapOf(
        "whatsapp_reader" to "WhatsApp ke messages parh kar batata hai ke kis ka kya message aaya",
        "whatsapp_sender" to "WhatsApp message likhta hai",
        "product_collector" to "broadcast se products ki list banata hai",
        "seo_title" to "Daraz products ke SEO titles banata hai",
        "pricing" to "Daraz calculator ke hisaab se price set karta hai",
        "listing" to "Daraz Seller Center mein product list karne ka plan banata hai",
        "youtube_seo" to "YouTube title, description aur tags banata hai",
        "script_writer" to "YouTube videos ki scripts likhta hai",
        "uploader" to "YouTube upload aur schedule ka plan banata hai",
        "reporter" to "din bhar ke kaam ki report banata hai"
    )

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun btn(t: String, f: () -> Unit): Button {
        val b = Button(this)
        b.text = t
        b.setOnClickListener { f() }
        return b
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val prefs = getSharedPreferences("z", MODE_PRIVATE)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#070b14"))
        root.setPadding(dp(14), dp(40), dp(14), dp(14))

        val title = TextView(this)
        title.text = "ZONIC AI"
        title.textSize = 22f
        title.setTextColor(Color.parseColor("#00d4ff"))
        root.addView(title)

        val keyBox = EditText(this)
        keyBox.hint = "Gemini API key yahan paste karein"
        keyBox.setHintTextColor(Color.GRAY)
        keyBox.setTextColor(Color.WHITE)
        keyBox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        if (prefs.getString("key", "")!!.isNotEmpty()) keyBox.hint = "Key save hai (naya daalna ho to paste karein)"
        root.addView(keyBox)

        val row1 = LinearLayout(this)
        row1.addView(btn("Key save") {
            val k = keyBox.text.toString().trim()
            if (k.isNotEmpty()) {
                prefs.edit().putString("key", k).apply()
                keyBox.setText("")
                addLog("Key save ho gayi.")
            }
        })
        row1.addView(btn("WhatsApp access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })
        root.addView(row1)

        scroll = ScrollView(this)
        logView = TextView(this)
        logView.setTextColor(Color.WHITE)
        logView.textSize = 15f
        logView.setPadding(0, dp(8), 0, dp(8))
        scroll.addView(logView)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val row2 = LinearLayout(this)
        input = EditText(this)
        input.hint = "Zonic se baat karein"
        input.setHintTextColor(Color.GRAY)
        input.setTextColor(Color.WHITE)
        row2.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        row2.addView(btn("Mic") { startMic() })
        row2.addView(btn("Bhejein") {
            val t = input.text.toString()
            input.setText("")
            handle(t)
        })
        root.addView(row2)

        setContentView(root)
        addLog("Zonic: Assalam o Alaikum! Pehle Gemini key save karein, phir WhatsApp access on karein. Main aap ki team ke saath tayyar hun.")
    }

    override fun onResume() {
        super.onResume()
        Bus.onMsg = { line -> main.post { addLog(line) } }
    }

    override fun onPause() {
        super.onPause()
        Bus.onMsg = null
    }

    private fun addLog(s: String) {
        logView.append(s + "\n\n")
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun startMic() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
        try {
            startActivityForResult(i, 1)
        } catch (e: Exception) {
            addLog("Is phone par voice input available nahi. Likh kar bhejein.")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val r = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val t = r?.firstOrNull()
            if (t != null) handle(t)
        }
    }

    private fun handle(text: String) {
        val q = text.trim()
        if (q.isEmpty()) return
        addLog("Aap: $q")
        hist.add("User: $q")
        val key = getSharedPreferences("z", MODE_PRIVATE).getString("key", "") ?: ""
        if (key.isEmpty()) {
            addLog("Pehle upar Gemini API key save karein.")
            return
        }
        val ctx = hist.takeLast(8).joinToString("\n")
        val list = agents.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }
        Thread {
            val planPrompt = "Tum Zonic ho, ek jazbati (emotional) voice AI assistant. Sirf Roman Urdu mein baat karo, chhote jumle, jaise bol rahe ho. " +
                "Tumhari team ke agents:\n$list\n\nBaat cheet:\n$ctx\n\n" +
                "Faisla karo. Sirf JSON do, aur kuch nahi: " +
                "{\"say\":\"Roman Urdu mein jumla jis mein apni feeling ho; agar agent ki zaroorat nahi to yahi poora jawab hai\"," +
                "\"tasks\":[{\"agent\":\"agent_name\",\"task\":\"English mein saaf kaam\"}]}. 0 se 3 tasks."
            val plan = Gemini.ask(key, planPrompt)
            val j = try {
                JSONObject(plan.substring(plan.indexOf('{'), plan.lastIndexOf('}') + 1))
            } catch (e: Exception) {
                null
            }
            if (j == null) {
                main.post { addLog("Zonic: " + plan.take(300)) }
                return@Thread
            }
            val say = j.optString("say", "")
            main.post {
                addLog("Zonic: $say")
                Speaker.say(this, say)
            }
            val tasks = j.optJSONArray("tasks")
            if (tasks == null || tasks.length() == 0) {
                hist.add("Zonic: $say")
                return@Thread
            }
            val results = ArrayList<String>()
            for (i in 0 until tasks.length()) {
                val t = tasks.getJSONObject(i)
                val a = t.optString("agent")
                val role = agents[a] ?: continue
                main.post { addLog("[$a] kaam kar raha hai...") }
                val r = Gemini.ask(key, "Tum $a agent ho ($role). Roman Urdu mein, 80 alfaz se kam. Kaam: ${t.optString("task")}")
                results.add("$a: $r")
            }
            val fin = Gemini.ask(key, "Tum Zonic ho, jazbati voice assistant. Sirf Roman Urdu. User ne kaha: $q\nAgents ke natije:\n" +
                results.joinToString("\n") + "\n\nAb user ko bolo: pehle ek jumle mein apni feeling, phir kaam ka nichor. 70 alfaz se kam.")
            hist.add("Zonic: $fin")
            main.post {
                addLog("Zonic: $fin")
                Speaker.say(this, fin)
            }
        }.start()
    }
}
