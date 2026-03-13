package br.com.insights.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Abstração central de qualquer compromisso financeiro processável
 * pelo motor de insights.
 */
interface Compromisso {
    val id: String
    val tipo: TipoCompromisso
    val dataVencimento: LocalDate
    val valor: BigDecimal
    val status: StatusCompromisso
    val cliente: Cliente
}

enum class TipoCompromisso {
    BOLETO,
    PARCELA_FINANCIAMENTO
}

enum class StatusCompromisso {
    PENDENTE,
    PAGO,
    VENCIDO,
    CANCELADO,
    RENEGOCIADO
}

/**
 * Value Object com dados do cliente — imutável e sem identidade própria.
 */
data class Cliente(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String
)
