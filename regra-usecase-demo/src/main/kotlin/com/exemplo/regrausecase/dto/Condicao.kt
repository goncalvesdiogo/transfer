package com.exemplo.regrausecase.dto

/**
 * Representa uma condicao individual de uma regra de caso de uso.
 * Ex: {"campo":"dt_vcto", "operacao": ">=", "valor": "$hoje"}
 */
data class Condicao(
    val campo: String,
    val operacao: String,
    val valor: String
)
