package com.exemplo.regrausecase.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Entidade de dominio simplificada, apenas para demonstrar a validacao
 * de condicoes vindas da regra contra um objeto real (nao e persistida).
 */
data class Boleto(
    val id: String,
    val dtVcto: LocalDate,
    val valor: BigDecimal,
    val tipo: String
)
