package com.exemplo.regrausecase.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

/**
 * Payload de entrada, no mesmo formato enviado pela base de casos de uso.
 */
data class RegraUseCasePayload(
    @field:NotBlank
    @JsonProperty("id_caso_uso")
    val idCasoUso: String,

    @field:NotEmpty
    @JsonProperty("aplicavelA")
    val aplicavelA: List<String>,

    @field:NotBlank
    @JsonProperty("usecase_name")
    val usecaseName: String,

    @JsonProperty("ativo")
    val ativo: Boolean = true,

    @field:NotEmpty
    @JsonProperty("condicoes")
    val condicoes: List<Condicao>,

    @field:NotBlank
    @JsonProperty("modo_disparo")
    val modoDisparo: String
)

/**
 * Resultado devolvido pelo endpoint de validacao.
 */
data class ResultadoValidacaoResponse(
    val idCasoUso: String,
    val usecaseName: String,
    val valido: Boolean,
    val motivo: String
)
