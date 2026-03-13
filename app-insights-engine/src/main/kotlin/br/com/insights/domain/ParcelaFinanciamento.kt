package br.com.insights.domain

import java.math.BigDecimal
import java.time.LocalDate

data class ParcelaFinanciamento(
    override val id: String,
    override val dataVencimento: LocalDate,
    override val valor: BigDecimal,
    override val status: StatusCompromisso,
    override val cliente: Cliente,
    val numeroParcela: Int,
    val totalParcelas: Int,
    val saldoDevedor: BigDecimal,
    val contratoId: String,
    val tipoBem: TipoBemFinanciado,
    val taxaJurosMensal: BigDecimal
) : Compromisso {
    override val tipo: TipoCompromisso = TipoCompromisso.PARCELA_FINANCIAMENTO

    val parcelasRestantes: Int get() = totalParcelas - numeroParcela
    val percentualQuitado: Double get() = (numeroParcela.toDouble() / totalParcelas) * 100
}

enum class TipoBemFinanciado {
    IMOVEL, VEICULO, EQUIPAMENTO, PESSOAL
}
