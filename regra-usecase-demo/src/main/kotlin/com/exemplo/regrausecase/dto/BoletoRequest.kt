package com.exemplo.regrausecase.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class BoletoRequest(
    @field:NotBlank
    val id: String,

    @field:NotNull
    val dtVcto: LocalDate,

    @field:NotNull
    val valor: BigDecimal,

    @field:NotBlank
    val tipo: String
)
