package br.com.insights.usecase.parcela

import br.com.insights.domain.*
import org.springframework.stereotype.Component

// ── D0 ────────────────────────────────────────────────────────────────────────

@Component
class ParcelaDueTodayUseCase : BaseParcelaUseCase() {

    override val name = "PARCELA_VENCE_HOJE_D0"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val p = context.parcela()
        return UseCaseResult.Success(name, listOf(
            buildEmail(p,
                subject  = "🚨 Parcela do financiamento vence HOJE",
                message  = "Olá, ${p.cliente.nome}! Parcela R$${p.valor} vence HOJE. " +
                           "Contrato: ${p.contratoId} | ${p.progresso()} | " +
                           "Saldo devedor: R$${p.saldoDevedor}",
                priority = NotificationPriority.CRITICAL,
                extra    = mapOf("contratoId" to p.contratoId)
            ),
            buildSms(p,
                message  = "HOJE vence parcela financiamento R$${p.valor}. " +
                           "Contrato: ${p.contratoId}. ${p.parcelasRestantes} parcelas restantes.",
                priority = NotificationPriority.CRITICAL
            ),
            buildWhatsApp(p,
                message  = "🚨 ${p.cliente.nome}, parcela R$${p.valor} vence HOJE! Evite negativação. 💳",
                priority = NotificationPriority.CRITICAL
            )
        ))
    }
}

// ── D3 ────────────────────────────────────────────────────────────────────────

@Component
class ParcelaDueInThreeDaysUseCase : BaseParcelaUseCase() {

    override val name = "PARCELA_VENCE_EM_3_DIAS_D3"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val p = context.parcela()
        return UseCaseResult.Success(name, listOf(
            buildEmail(p,
                subject  = "Lembrete: parcela vence em 3 dias",
                message  = "Olá, ${p.cliente.nome}! Parcela R$${p.valor} vence em " +
                           "${p.dataVencimento}. ${p.progresso()} | Saldo: R$${p.saldoDevedor}",
                priority = NotificationPriority.HIGH,
                extra    = mapOf("contratoId" to p.contratoId)
            ),
            buildSms(p,
                message  = "Parcela R$${p.valor} vence em 3 dias (${p.dataVencimento}). " +
                           "Contrato: ${p.contratoId}.",
                priority = NotificationPriority.HIGH
            )
        ))
    }
}

// ── D7 ────────────────────────────────────────────────────────────────────────

@Component
class ParcelaDueInSevenDaysUseCase : BaseParcelaUseCase() {

    override val name = "PARCELA_VENCE_EM_7_DIAS_D7"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val p = context.parcela()
        return UseCaseResult.Success(name, listOf(
            buildEmail(p,
                subject = "Aviso: parcela de financiamento vence em 7 dias",
                message = "Olá, ${p.cliente.nome}! Parcela R$${p.valor} vence em " +
                          "${p.dataVencimento}. Contrato: ${p.contratoId} (${p.tipoBem.name}) | " +
                          "${p.progresso()} | Taxa: ${p.taxaJurosMensal}% a.m.",
                extra   = mapOf("contratoId" to p.contratoId, "tipoBem" to p.tipoBem.name)
            )
        ))
    }
}
