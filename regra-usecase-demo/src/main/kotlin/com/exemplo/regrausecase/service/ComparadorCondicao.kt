package com.exemplo.regrausecase.service

import org.springframework.stereotype.Component

@Component
class ComparadorCondicao {

    @Suppress("UNCHECKED_CAST")
    fun avaliar(valorCampo: Any?, operacao: String, valorEsperado: Any?): Boolean {
        if (valorCampo == null) return false
        requireNotNull(valorEsperado) { "Valor esperado nulo para operacao '$operacao'" }

        return when (operacao) {
            "=", "==" -> valorCampo == valorEsperado
            "!=" -> valorCampo != valorEsperado
            ">=", "<=", ">", "<" -> {
                check(valorCampo is Comparable<*>) {
                    "Campo do tipo ${valorCampo::class.simpleName} nao e comparavel"
                }
                val resultado = (valorCampo as Comparable<Any>).compareTo(valorEsperado)
                when (operacao) {
                    ">=" -> resultado >= 0
                    "<=" -> resultado <= 0
                    ">" -> resultado > 0
                    else -> resultado < 0
                }
            }
            "IN" -> (valorEsperado as? Collection<*>)?.contains(valorCampo) ?: false
            else -> throw IllegalArgumentException("Operacao nao suportada: $operacao")
        }
    }
}
