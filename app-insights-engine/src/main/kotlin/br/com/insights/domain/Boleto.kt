package br.com.insights.domain

import java.math.BigDecimal
import java.time.LocalDate

data class Boleto(
    override val id: String,
    override val dataVencimento: LocalDate,
    override val valor: BigDecimal,
    override val status: StatusCompromisso,
    override val cliente: Cliente,
    val linhaDigitavel: String,
    val banco: String,
    val nossoNumero: String
) : Compromisso {
    override val tipo: TipoCompromisso = TipoCompromisso.BOLETO
}
