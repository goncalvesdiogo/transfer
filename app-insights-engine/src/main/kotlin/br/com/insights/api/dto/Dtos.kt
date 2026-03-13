package br.com.insights.api.dto

import br.com.insights.domain.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

// ── Request ───────────────────────────────────────────────────────────────────

data class ProcessRequest(
    @field:NotNull val tipo: TipoCompromisso,
    @field:NotBlank val id: String,
    @field:NotNull val dataVencimento: LocalDate,
    @field:Positive val valor: BigDecimal,
    @field:NotNull val status: StatusCompromisso,
    @field:Valid val cliente: ClienteDto,
    val dadosBoleto: BoletoDto? = null,
    val dadosParcela: ParcelaDto? = null
)

data class ClienteDto(
    @field:NotBlank val id: String,
    @field:NotBlank val nome: String,
    @field:NotBlank val email: String,
    @field:NotBlank val telefone: String
)

data class BoletoDto(
    @field:NotBlank val linhaDigitavel: String,
    @field:NotBlank val banco: String,
    @field:NotBlank val nossoNumero: String
)

data class ParcelaDto(
    @field:Positive val numeroParcela: Int,
    @field:Positive val totalParcelas: Int,
    @field:Positive val saldoDevedor: BigDecimal,
    @field:NotBlank val contratoId: String,
    @field:NotNull val tipoBem: TipoBemFinanciado,
    @field:Positive val taxaJurosMensal: BigDecimal
)

data class BatchProcessRequest(
    @field:Valid val compromissos: List<ProcessRequest>
)

// ── Response ──────────────────────────────────────────────────────────────────

data class ProcessResponse(
    val status: String,
    val useCaseName: String,
    val notificationsDispatched: Int = 0,
    val message: String
)

data class BatchProcessResponse(
    val total: Int,
    val success: Int,
    val skipped: Int,
    val failures: Int
)

// ── Mappers ───────────────────────────────────────────────────────────────────

fun ProcessRequest.toCompromisso(): Compromisso {
    val cliente = Cliente(cliente.id, cliente.nome, cliente.email, cliente.telefone)
    return when (tipo) {
        TipoCompromisso.BOLETO -> {
            val d = requireNotNull(dadosBoleto) { "dadosBoleto obrigatório para BOLETO" }
            Boleto(id, dataVencimento, valor, status, cliente, d.linhaDigitavel, d.banco, d.nossoNumero)
        }
        TipoCompromisso.PARCELA_FINANCIAMENTO -> {
            val d = requireNotNull(dadosParcela) { "dadosParcela obrigatório para PARCELA_FINANCIAMENTO" }
            ParcelaFinanciamento(id, dataVencimento, valor, status, cliente,
                d.numeroParcela, d.totalParcelas, d.saldoDevedor, d.contratoId, d.tipoBem, d.taxaJurosMensal)
        }
    }
}
