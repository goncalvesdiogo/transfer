package br.com.insights.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Contexto de processamento — transporta o compromisso e metadados
 * computados ao longo de toda a pipeline de decisão.
 *
 * Pattern: Context Object / Parameter Object
 */
data class ProcessingContext(
    val compromisso: Compromisso,
    val dataReferencia: LocalDate = LocalDate.now(),
    val atributos: MutableMap<String, Any> = mutableMapOf()
) {
    val diasAteVencimento: Long
        get() = ChronoUnit.DAYS.between(dataReferencia, compromisso.dataVencimento)

    val tipoCompromisso: TipoCompromisso
        get() = compromisso.tipo

    fun setAttribute(key: String, value: Any) { atributos[key] = value }

    @Suppress("UNCHECKED_CAST")
    fun <T> getAttribute(key: String): T? = atributos[key] as? T

    inline fun <reified T : Compromisso> compromissoAs(): T =
        compromisso as? T
            ?: error(
                "Cast inválido: esperado ${T::class.simpleName}, " +
                "recebido ${compromisso::class.simpleName}"
            )
}
