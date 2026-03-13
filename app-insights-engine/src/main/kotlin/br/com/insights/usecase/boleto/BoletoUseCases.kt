package br.com.insights.usecase.boleto

import br.com.insights.domain.*
import org.springframework.stereotype.Component

// ── D0 ────────────────────────────────────────────────────────────────────────

@Component
class BoletoDueTodayUseCase : BaseBoletoUseCase() {

    override val name = "BOLETO_VENCE_HOJE_D0"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val b = context.boleto()
        return UseCaseResult.Success(name, listOf(
            buildEmail(b,
                subject  = "⚠️ Seu boleto vence HOJE!",
                message  = "Olá, ${b.cliente.nome}! Seu boleto de R$${b.valor} vence HOJE " +
                           "(${b.dataVencimento}). Banco: ${b.banco} | ${b.linhaDigitavel}",
                priority = NotificationPriority.CRITICAL
            ),
            buildSms(b,
                message  = "HOJE vence seu boleto R$${b.valor}. Pague para evitar multa. Banco: ${b.banco}",
                priority = NotificationPriority.CRITICAL
            ),
            buildWhatsApp(b,
                message  = "⚠️ ${b.cliente.nome}, boleto R$${b.valor} vence HOJE! 🔔",
                priority = NotificationPriority.CRITICAL
            )
        ))
    }
}

// ── D4 ────────────────────────────────────────────────────────────────────────

@Component
class BoletoDueInFourDaysUseCase : BaseBoletoUseCase() {

    override val name = "BOLETO_VENCE_EM_4_DIAS_D4"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val b = context.boleto()
        return UseCaseResult.Success(name, listOf(
            buildEmail(b,
                subject = "Lembrete: boleto vence em 4 dias",
                message = "Olá, ${b.cliente.nome}! Boleto R$${b.valor} vence em " +
                          "${b.dataVencimento}. Banco: ${b.banco}"
            ),
            buildSms(b,
                message = "Lembrete: boleto R$${b.valor} vence em 4 dias (${b.dataVencimento})."
            )
        ))
    }
}

// ── D10 ───────────────────────────────────────────────────────────────────────

@Component
class BoletoDueInTenDaysUseCase : BaseBoletoUseCase() {

    override val name = "BOLETO_VENCE_EM_10_DIAS_D10"

    override fun doExecute(context: ProcessingContext): UseCaseResult {
        val b = context.boleto()
        return UseCaseResult.Success(name, listOf(
            buildEmail(b,
                subject  = "Aviso: boleto vence em 10 dias",
                message  = "Olá, ${b.cliente.nome}! Boleto R$${b.valor} vence em " +
                           "${b.dataVencimento}. Banco: ${b.banco}",
                priority = NotificationPriority.LOW
            )
        ))
    }
}
