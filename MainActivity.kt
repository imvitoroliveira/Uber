package com.metauber.filtro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {
    private val fields = listOf(
        "kmIdeal" to "IDEAL: R$/km mínimo (meta completa)",
        "kmRuim" to "RUIM: abaixo de R$/km",
        "minIdeal" to "IDEAL: R$/min mínimo",
        "minRuim" to "RUIM: abaixo de R$/min"
    )
    private val inputs = mutableMapOf<String, EditText>()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        root.addView(TextView(this).apply { text = "Filtro de corridas"; textSize = 22f })
        for ((k, label) in fields) {
            root.addView(TextView(this).apply { text = label })
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(Cfg.get(this@MainActivity, k).toString().replace('.', ','))
            }
            inputs[k] = et
            root.addView(et)
        }
        root.addView(Button(this).apply { text = "Salvar metas"; setOnClickListener { save() } })
        root.addView(Button(this).apply {
            text = "Ativar serviço (Acessibilidade)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(TextView(this).apply {
            text = "Em Acessibilidade > Apps instalados, ative \"Filtro de Corridas\". " +
                "O selo aparece sobre a oferta da Uber: verde = aceitar, amarelo = média, vermelho = recusar."
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun save() {
        for ((k, et) in inputs) {
            et.text.toString().replace(',', '.').toFloatOrNull()?.let { Cfg.set(this, k, it) }
        }
        Toast.makeText(this, "Salvo", Toast.LENGTH_SHORT).show()
    }
}
