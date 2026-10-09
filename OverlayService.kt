package com.metauber.filtro

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.util.Locale

class OverlayService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var tv: TextView? = null
    private var last = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val hideRun = Runnable { hide() }

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        val now = SystemClock.uptimeMillis()
        if (now - last < 300) return
        last = now
        val root = rootInActiveWindow ?: return
        val texts = ArrayList<String>()
        collect(root, texts)
        // Para ajustar o leitor: Logcat, filtro "UberFiltro"
        Log.d("UberFiltro", texts.joinToString(" | "))
        val offer = Parser.parse(texts)
        if (offer == null) hide() else show(offer)
    }

    private fun collect(n: AccessibilityNodeInfo?, out: MutableList<String>) {
        if (n == null) return
        n.text?.let { out.add(it.toString()) }
        n.contentDescription?.let { out.add(it.toString()) }
        for (i in 0 until n.childCount) collect(n.getChild(i), out)
    }

    private fun show(o: Offer) {
        val c = applicationContext
        val perKm = o.price / o.km
        val perMin = o.price / o.min
        val kmI = Cfg.get(c, "kmIdeal").toDouble()
        val kmR = Cfg.get(c, "kmRuim").toDouble()
        val minI = Cfg.get(c, "minIdeal").toDouble()
        val minR = Cfg.get(c, "minRuim").toDouble()
        // Regra do plano: nenhum critério "ruim" + pelo menos um "ideal" = aceitar
        val (label, color) = when {
            perKm < kmR || perMin < minR -> "RECUSAR" to 0xFFC62828.toInt()
            perKm >= kmI || perMin >= minI -> "ACEITAR" to 0xFF2E7D32.toInt()
            else -> "MÉDIA" to 0xFFF9A825.toInt()
        }
        val text = String.format(Locale("pt", "BR"), "%s\nR\$ %.2f/km · R\$ %.2f/min", label, perKm, perMin)
        val v = tv ?: TextView(c).also {
            it.setTextColor(Color.WHITE)
            it.textSize = 20f
            it.gravity = Gravity.CENTER
            it.setPadding(48, 24, 48, 24)
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            lp.y = 160
            wm?.addView(it, lp)
            tv = it
        }
        v.text = text
        v.background = GradientDrawable().apply { setColor(color); cornerRadius = 32f }
        handler.removeCallbacks(hideRun)
        handler.postDelayed(hideRun, 15000)
    }

    private fun hide() {
        tv?.let {
            try { wm?.removeView(it) } catch (_: Exception) {}
            tv = null
        }
    }

    override fun onInterrupt() { hide() }
    override fun onDestroy() { hide(); super.onDestroy() }
}
