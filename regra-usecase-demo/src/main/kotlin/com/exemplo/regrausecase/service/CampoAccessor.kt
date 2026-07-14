package com.exemplo.regrausecase.service

import org.springframework.stereotype.Component
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Component
class CampoAccessor {

    fun obterValor(entidade: Any, campo: String): Any? {
        val nomePropriedade = campo.toCamelCase()
        val propriedade = entidade::class.memberProperties
            .find { it.name == nomePropriedade }
            ?: throw IllegalArgumentException(
                "Campo '$campo' (esperado como '$nomePropriedade') nao encontrado em ${entidade::class.simpleName}"
            )

        @Suppress("UNCHECKED_CAST")
        return (propriedade as KProperty1<Any, *>).get(entidade)
    }

    private fun String.toCamelCase(): String =
        split("_")
            .mapIndexed { index, parte -> if (index == 0) parte else parte.replaceFirstChar { it.uppercase() } }
            .joinToString("")
}
