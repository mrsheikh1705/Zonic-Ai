package com.zonic.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AgentDef(val id: String, val label: String, val role: String, val color: Int)

val AGENTS = listOf(
    AgentDef("whatsapp_reader", "Reader", "WhatsApp ke messages parh kar batata hai ke kis ka kya aaya", 0xFF25D366.toInt()),
    AgentDef("whatsapp_sender", "Sender", "WhatsApp par kisi ko message bhejta hai (contact aur message chahiye)", 0xFF34B7F1.toInt()),
    AgentDef("product_collector", "Collect", "broadcast se products aur unki prices ki list banata hai", 0xFFFF9F43.toInt()),
    AgentDef("seo_title", "SEO", "Daraz products ke SEO titles banata hai", 0xFFA06BFF.toInt()),
    AgentDef("pricing", "Price", "Daraz calculator ke hisaab se selling price nikalta hai", 0xFFFFD166.toInt()),
    AgentDef("listing", "Listing", "Daraz Seller Center mein product list karne ka kaam sambhalta hai", 0xFFFF7AC8.toInt()),
    AgentDef("youtube_seo", "YT SEO", "YouTube title, description aur tags banata hai", 0xFFFF4D6D.toInt()),
    AgentDef("script_writer", "Script", "YouTube videos ki scripts likhta hai", 0xFF00D4FF.toInt()),
    AgentDef("uploader", "Upload", "YouTube upload aur schedule ka plan banata hai", 0xFF3DDC97.toInt()),
    AgentDef("reporter", "Report", "din bhar ke kaam ki report banata hai", 0xFFC8D3F5.toInt())
)

class OrbView(c: Context) : View(c) {
    var state = 0
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(cv: Canvas) {
        val t = System.currentTimeMillis() / 1000.0
        val cx = width / 2f
        val cy = height / 2f
        val base = minOf(width, height) * 0.26f
        val col = when (state) {
            1 -> 0xFF3DDC97.toInt()
            2 -> 0xFFA06BFF.toInt()
            3 -> 0xFFFF7AC8.toInt()
            else -> 0xFF00D4FF.toInt()
        }
        val speed = if (state == 0) 0.6 else 2.2
        val pulse = (1 + 0.08 * sin(t * speed * 3)).toFloat()
        p.style = Paint.Style.FILL
        for (i in 4 downTo 1) {
            p.shader = null
            p.color = col
            p.alpha = 16 * (5 - i)
            cv.drawCircle(cx, cy, base * pulse * (1 + i * 0.25f), p)
        }
        p.alpha = 255
        p.shader = RadialGradient(cx, cy, base * pulse, intArrayOf(Color.WHITE, col, 0xFF001A2C.toInt()), floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP)
        cv.drawCircle(cx, cy, base * pulse, p)
        p.shader = null
        p.color = col
        for (k in 0 until 48) {
            val a = k * 2 * PI / 48 + t * speed * 0.5
            val r = base * 1.7f + 5f * sin(k + t * 3).toFloat()
            p.alpha = 200
            cv.drawCircle(cx + (r * cos(a)).toFloat(), cy + (r * sin(a)).toFloat(), 3f, p)
        }
        postInvalidateOnAnimation()
    }
}

class OfficeView(c: Context) : View(c) {
    val st = HashMap<String, Int>()
    private val p = Paint()

    init {
        p.isAntiAlias = false
    }

    override fun onMeasure(w: Int, h: Int) {
        val ww = MeasureSpec.getSize(w)
        setMeasuredDimension(ww, (ww * 0.5f).toInt())
    }

    override fun onDraw(cv: Canvas) {
        val t = System.currentTimeMillis() / 1000.0
        cv.drawColor(0xFF0F1830.toInt())
        p.style = Paint.Style.FILL
        p.color = 0xFF16233F.toInt()
        var gx = 0f
        while (gx < width) {
            cv.drawRect(gx, 0f, gx + 2f, height.toFloat(), p)
            gx += 40f
        }
        var gy = 0f
        while (gy < height) {
            cv.drawRect(0f, gy, width.toFloat(), gy + 2f, p)
            gy += 40f
        }
        val cw = width / 5f
        val rh = height / 2f
        val u = cw / 10f
        for ((i, ag) in AGENTS.withIndex()) {
            val s = st[ag.id] ?: 0
            val ox = (i % 5) * cw
            val oy = (i / 5) * rh
            val mid = ox + cw / 2f
            val bob = if (s == 1) (sin(t * 10 + i) * u * 0.3).toFloat() else 0f
            val headTop = oy + rh * 0.10f + bob
            p.color = ag.color
            cv.drawRect(mid - u * 1.3f, headTop + u * 1.6f, mid + u * 1.3f, headTop + u * 4f, p)
            p.color = 0xFFF2C9A0.toInt()
            cv.drawRect(mid - u * 0.8f, headTop, mid + u * 0.8f, headTop + u * 1.6f, p)
            p.color = 0xFF2A1E16.toInt()
            cv.drawRect(mid - u * 0.8f, headTop, mid + u * 0.8f, headTop + u * 0.6f, p)
            p.color = 0xFF6B4F35.toInt()
            cv.drawRect(ox + u, oy + rh * 0.50f, ox + cw - u, oy + rh * 0.50f + u * 2.2f, p)
            p.color = 0xFF0A0F1C.toInt()
            cv.drawRect(mid - u * 1.7f, oy + rh * 0.34f, mid + u * 1.7f, oy + rh * 0.50f, p)
            p.color = when (s) {
                1 -> if (sin(t * 12 + i) > 0) 0xFF00D4FF.toInt() else 0xFFBFF4FF.toInt()
                2 -> 0xFF3DDC97.toInt()
                else -> 0xFF1D2B45.toInt()
            }
            cv.drawRect(mid - u * 1.4f, oy + rh * 0.37f, mid + u * 1.4f, oy + rh * 0.48f, p)
            p.textAlign = Paint.Align.CENTER
            p.textSize = u * 1.9f
            p.color = if (s == 0) 0xFF7F8FB0.toInt() else Color.WHITE
            cv.drawText(ag.label, mid, oy + rh * 0.50f + u * 4.2f, p)
            if (s == 1) {
                p.color = Color.WHITE
                p.textSize = u * 2.6f
                cv.drawText("...", mid + u * 2.6f, headTop + u * 0.6f, p)
            } else if (s == 2) {
                p.color = 0xFF3DDC97.toInt()
                p.textSize = u * 2.6f
                cv.drawText("OK", mid + u * 2.6f, headTop + u * 0.6f, p)
            }
        }
        postInvalidateOnAnimation()
    }
}
