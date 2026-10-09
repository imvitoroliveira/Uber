package com.metauber.filtro

import android.content.Context

object Cfg {
    // Padrões = tabela do plano (R$/km e R$/min, já incluindo km/min da busca)
    private val defaults = mapOf("kmIdeal" to 1.65f, "kmRuim" to 1.30f, "minIdeal" to 0.65f, "minRuim" to 0.50f)
    private fun p(c: Context) = c.getSharedPreferences("cfg", Context.MODE_PRIVATE)
    fun get(c: Context, k: String): Float = p(c).getFloat(k, defaults.getValue(k))
    fun set(c: Context, k: String, v: Float) = p(c).edit().putFloat(k, v).apply()
}
