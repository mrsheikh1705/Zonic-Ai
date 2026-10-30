package com.zonic.ai

import org.json.JSONObject

object PhoneAgent {
    @Volatile
    var cancel = false

    fun run(key: String, goal: String, step: (String) -> Unit): String {
        cancel = false
        val acc = ZonicAccess.inst ?: return "ERR: Phone control (Accessibility) on nahi hai"
        val hist = ArrayList<String>()
        for (n in 1..22) {
            if (cancel) return "ERR: Kaam rok diya gaya"
            val screen = acc.dump()
            val past = if (hist.isEmpty()) "(abhi koi nahi)" else hist.takeLast(8).joinToString("\n")
            val prompt = """Tum ek Android phone chalane wale agent ho. Tumhara kaam (goal): $goal

Abhi phone ki screen par yeh hai (har line ka number [i] hai):
$screen

Ab tak ke qadam:
$past

Sirf EK JSON do, aur kuch nahi:
{"say":"chhota Roman Urdu jumla ke ab kya kar rahe ho","action":"open_app|click|type|scroll_down|scroll_up|back|home|wait|done|fail","index":0,"text":"","app":""}
Qaide:
- Shuru mein open_app se zaroori app kholo (app mein app ka naam likho, jaise WhatsApp).
- click ke liye index do. type ke liye EDIT field ka index aur text do.
- WhatsApp message: WhatsApp kholo, search icon dabao, naam type karo, sahi chat kholo, message box mein type karo, Send dabao, phir done.
- Agar pichla qadam nakam raha to doosra tareeqa try karo, wohi qadam baar baar mat dohrao.
- Kabhi payment, password badalna, account ya chat delete mat karo.
- Kaam poora hone par action=done aur text mein natija (Roman Urdu mein, ke kya kiya ya kya mila). Agar na ho sake to action=fail aur text mein wajah."""
            val out = Gemini.ask(key, prompt)
            if (out.startsWith("ERR:")) return out
            val j = try {
                JSONObject(out.substring(out.indexOf('{'), out.lastIndexOf('}') + 1))
            } catch (e: Exception) {
                hist.add("(jawab ka format galat tha)")
                continue
            }
            val say = j.optString("say", "")
            if (say.isNotEmpty()) step(say)
            val act = j.optString("action", "")
            var ok = true
            var wait = 1400L
            when (act) {
                "open_app" -> { ok = acc.openApp(j.optString("app", "")); wait = 2800L }
                "click" -> ok = acc.click(j.optInt("index", -1))
                "type" -> ok = acc.type(j.optInt("index", -1), j.optString("text", ""))
                "scroll_down" -> ok = acc.scroll(true)
                "scroll_up" -> ok = acc.scroll(false)
                "back" -> ok = acc.back()
                "home" -> ok = acc.home()
                "wait" -> wait = 2000L
                "done" -> return j.optString("text", "Kaam mukammal")
                "fail" -> return "ERR: " + j.optString("text", "kaam nahi ho saka")
                else -> ok = false
            }
            hist.add(act + " index=" + j.optString("index", "") + " text=" + j.optString("text", "") + " app=" + j.optString("app", "") + " -> " + (if (ok) "ok" else "nakam"))
            Thread.sleep(wait)
        }
        return "ERR: Qadam bohat zyada ho gaye, kaam poora nahi hua"
    }
}
