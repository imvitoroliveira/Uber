package com.metauber.filtro

data class Offer(val price: Double, val km: Double, val min: Double)

object Parser {
    // Preço da oferta: nó com texto exatamente "R$ 18,83" (ignora "+R$ 3,71 incluído...")
    private val priceRe = Regex("""^R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2})$""")
    // Trechos "4 minutos (1,4 km)" -> 1º = busca, 2º = viagem
    private val legRe = Regex("""(\d+)\s*min\w*\s*\(\s*(\d+(?:[.,]\d+)?)\s*(km|m)\s*\)""", RegexOption.IGNORE_CASE)

    fun parse(raw: List<String>): Offer? {
        val texts = raw.map { it.replace('\u00A0', ' ').trim() }
        val price = texts.firstNotNullOfOrNull { priceRe.find(it) }
            ?.groupValues?.get(1)?.replace(".", "")?.replace(",", ".")?.toDoubleOrNull() ?: return null
        val legs = texts.flatMap { legRe.findAll(it).toList() }.take(2)
        if (legs.isEmpty()) return null
        var km = 0.0
        var min = 0.0
        for (m in legs) {
            min += m.groupValues[1].toDouble()
            val d = m.groupValues[2].replace(",", ".").toDouble()
            km += if (m.groupValues[3].lowercase() == "m") d / 1000 else d
        }
        return if (km > 0 && min > 0) Offer(price, km, min) else null
    }
}
