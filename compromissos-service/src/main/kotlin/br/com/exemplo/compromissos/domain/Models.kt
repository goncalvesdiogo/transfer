package br.com.exemplo.compromissos.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

// ---------------------------------------------------------------------------
// Domain — Compromisso
// ---------------------------------------------------------------------------

data class Compromisso(
    val idCompromisso: UUID,
    val dataVencimento: LocalDate,
    val nomeBeneficiario: String,
    val valor: BigDecimal,
    val metadado: String
)

// ---------------------------------------------------------------------------
// Domain — Notificacao (entidade que vai ao DynamoDB)
// ---------------------------------------------------------------------------

data class Notificacao(
    val idNotificacao: UUID,
    val idCompromisso: UUID,
    val dataVencimento: LocalDate,
    val nomeBeneficiario: String,
    val valor: BigDecimal,
    val metadado: String,
    val dataPublicacao: LocalDate,
    /** Identifica qual caso de uso gerou a notificação */
    val useCaseId: String
) {
    /**
     * Sort Key composta: idNotificacao#dataVencimento
     * Facilita queries por data de vencimento dentro de uma partição.
     */
    val sk: String
        get() = "$idNotificacao#$dataVencimento"
}

// ---------------------------------------------------------------------------
// Domain — Resultado de elegibilidade
// ---------------------------------------------------------------------------

data class UseCaseResult(
    val useCaseId: String,
    val eligible: Boolean,
    val reason: String? = null
)
