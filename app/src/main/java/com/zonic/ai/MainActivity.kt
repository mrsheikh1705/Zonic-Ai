package com.zonic.ai

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit.SharedPrefrences sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Stonic AI Style Dynamic UI Layout in Kotlin to prevent any XML folder errors
        val linearLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#0B0F19"))
            setPadding(32, 32, 32, 32)
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Title Header
        val titleText = TextView(this).apply {
            text = "ZONIC AI (JARVIS)"
            textSize = 20f
            setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        linearLayout.addView(titleText)

        // Subtitle Status
        val statusText = TextView(this).apply {
            text = "Autonomous Multi-Agent Online"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#00E676"))
            setPadding(0, 0, 0, 24)
        }
        linearLayout.addView(statusText)

        // API Key Input Layout
        val keyLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val apiKeyInput = EditText(this).apply {
            hint = "Gemini API Key yahan dalein..."
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#757575"))
            textSize = 13f
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        keyLayout.addView(apiKeyInput)

        val btnSaveKey = Button(this).apply {
            text = "SAVE"
            setBackgroundColor(android.graphics.Color.parseColor("#00E5FF"))
            setTextColor(android.graphics.Color.parseColor("#0B0F19"))
        }
        keyLayout.addView(btnSaveKey)
        linearLayout.addView(keyLayout)

        // Chat Response Box inside ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                setMargins(0, 24, 0, 24)
            }
        }

        val chatResponseBox = TextView(this).apply {
            text = "Zonic: Assalam o Alaikum! Main aapka autonomous assistant hoon. WhatsApp, Daraz, aur baqi saare tasks ke liye tayyar hoon. API key enter karein aur mujhe hukm dein!"
            setTextColor(android.graphics.Color.parseColor("#E0E0E0"))
            textSize = 14f
        }
        scrollView.addView(chatResponseBox)
        linearLayout.addView(scrollView)

        // User Input & Send Layout
        val inputLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val userInputBox = EditText(this).apply {
            hint = "Zonic ko hukum dein..."
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#757575"))
            textSize = 14f
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        inputLayout.addView(userInputBox)

        val btnSend = Button(this).apply {
            text = "SEND"
            setBackgroundColor(android.graphics.Color.parseColor("#00E5FF"))
            setTextColor(android.graphics.Color.parseColor("#0B0F19"))
        }
        inputLayout.addView(btnSend)
        linearLayout.addView(inputLayout)

        // Set Content View directly via Code (No XML layout file needed, zero error guarantee!)
        setContentView(linearLayout)

        // SharedPreferences for API Key Saving
        val prefs = getSharedPreferences("ZonicPrefs", MODE_PRIVATE)
        val savedKey = prefs.getString("
