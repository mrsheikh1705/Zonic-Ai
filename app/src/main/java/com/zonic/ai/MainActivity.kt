package com.zonic.ai

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0B0F19"))
            setPadding(32, 32, 32, 32)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val titleText = TextView(this).apply {
            text = "ZONIC AI (JARVIS)"
            textSize = 20f
            setTextColor(Color.parseColor("#00E5FF"))
            setTypeface(null, Typeface.BOLD)
        }
        linearLayout.addView(titleText)

        val statusText = TextView(this).apply {
            text = "Autonomous Multi-Agent Online"
            textSize = 12f
            setTextColor(Color.parseColor("#00E676"))
            setPadding(0, 0, 0, 24)
        }
        linearLayout.addView(statusText)

        val keyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val apiKeyInput = EditText(this).apply {
            hint = "Gemini API Key yahan dalein..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#757575"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        keyLayout.addView(apiKeyInput)

        val btnSaveKey = Button(this).apply {
            text = "SAVE"
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.parseColor("#0B0F19"))
        }
        keyLayout.addView(btnSaveKey)
        linearLayout.addView(keyLayout)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                setMargins(0, 24, 0, 24)
            }
        }

        val chatResponseBox = TextView(this).apply {
            text = "Zonic: Assalam o Alaikum! Main aapka autonomous assistant hoon. WhatsApp, Daraz, aur baqi saare tasks ke liye tayyar hoon. API key enter karein aur mujhe hukm dein!"
            setTextColor(Color.parseColor("#E0E0E0"))
            textSize = 14f
        }
        scrollView.addView(chatResponseBox)
        linearLayout.addView(scrollView)

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val userInputBox = EditText(this).apply {
            hint = "Zonic ko hukum dein..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#757575"))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        inputLayout.addView(userInputBox)

        val btnSend = Button(this).apply {
            text = "SEND"
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.parseColor("#0B0F19"))
        }
        inputLayout.addView(btnSend)
        linearLayout.addView(inputLayout)

        setContentView(linearLayout)

        sharedPreferences = getSharedPreferences("ZonicPrefs", MODE_PRIVATE)
        val savedKey = sharedPreferences.getString("gemini_key", "")
        if (!savedKey.isNullOrEmpty()) {
            apiKeyInput.setText(savedKey)
            chatResponseBox.text = "Zonic: API Key pehle se saved hai. Hukam dein!"
        }

        btnSaveKey.setOnClickListener {
            val key = apiKeyInput.text.toString().trim()
            if (key.isNotEmpty()) {
                sharedPreferences.edit().putString("gemini_key", key).apply()
                Toast.makeText(this, "API Key Save Ho Gayi!", Toast.LENGTH_SHORT).show()
                chatResponseBox.text = "Zonic: Key save ho chuki hai. Ab aap WhatsApp, Daraz ya kisi bhi kaam ke liye keh sakte hain."
            } else {
                Toast.makeText(this, "Pehle valid key dalein!", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            val query = userInputBox.text.toString().trim()
            if (query.isNotEmpty()) {
                chatResponseBox.append("\n\nYou: $query")
                chatResponseBox.append("\n\nZonic: Aapka hukm mil gaya hai. ' $query ' par kaam shuru kiya ja raha hai...")
                userInputBox.text.clear()
            }
        }
    }
}
