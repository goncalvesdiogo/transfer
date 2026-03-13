package br.com.insights.usecase

import br.com.insights.domain.*
import org.slf4j.LoggerFactory

/**
 * Base para todos os use cases — provê Template Method com tratamento
 * de erro e builders de notificação reutilizáveis para qualquer [Compromisso].
 *
 * Pattern: Template Method
 */
abstract class BaseNotificationUseCase : NotificationUseCase {

    protected val log = LoggerFactory.getLogger(javaClass)

    final override fun execute(context: ProcessingContext): UseCaseResult {
        return try {
            if (!shouldProcess(context)) {
                return UseCaseResult.Skipped(name, "Pré-condição não atendida para [$name]")
            }
            log.debug("Executando use case [{}] para compromisso [{}]", name, context.compromisso.id)
            doExecute(context)
        } catch (ex: Exception) {
            log.error("Falha no use case [{}]: {}", name, ex.message, ex)
            UseCaseResult.Failure(name, ex)
        }
    }

    protected open fun shouldProcess(context: ProcessingContext): Boolean = true
    protected abstract fun doExecute(context: ProcessingContext): UseCaseResult

    // ── Builders ──────────────────────────────────────────────────────────────

    protected fun buildEmail(
        compromisso: Compromisso,
        subject: String,
        message: String,
        priority: NotificationPriority = NotificationPriority.MEDIUM,
        extra: Map<String, Any> = emptyMap()
    ) = Notification(
        recipientId    = compromisso.cliente.id,
        recipientEmail = compromisso.cliente.email,
        recipientPhone = compromisso.cliente.telefone,
        subject = subject, message = message,
        channel  = NotificationChannel.EMAIL, priority = priority,
        metadata = baseMeta(compromisso) + extra
    )

    protected fun buildSms(
        compromisso: Compromisso,
        message: String,
        priority: NotificationPriority = NotificationPriority.MEDIUM,
        extra: Map<String, Any> = emptyMap()
    ) = Notification(
        recipientId    = compromisso.cliente.id,
        recipientEmail = compromisso.cliente.email,
        recipientPhone = compromisso.cliente.telefone,
        subject = "", message = message,
        channel  = NotificationChannel.SMS, priority = priority,
        metadata = baseMeta(compromisso) + extra
    )

    protected fun buildWhatsApp(
        compromisso: Compromisso,
        message: String,
        priority: NotificationPriority = NotificationPriority.HIGH,
        extra: Map<String, Any> = emptyMap()
    ) = Notification(
        recipientId    = compromisso.cliente.id,
        recipientEmail = compromisso.cliente.email,
        recipientPhone = compromisso.cliente.telefone,
        subject = "", message = message,
        channel  = NotificationChannel.WHATSAPP, priority = priority,
        metadata = baseMeta(compromisso) + extra
    )

    private fun baseMeta(c: Compromisso): Map<String, Any> = mapOf(
        "compromissoId"  to c.id,
        "tipo"           to c.tipo.name,
        "dataVencimento" to c.dataVencimento.toString(),
        "valor"          to c.valor.toPlainString()
    )
}
