package br.com.insights.gateway

import br.com.insights.domain.Notification
import br.com.insights.domain.NotificationChannel
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Adapter padrão — registra notificações no log estruturado.
 * Ativo quando insights.engine.notification.dry-run=true
 * ou como fallback de desenvolvimento.
 *
 * Substitua por SendGridGateway, TwilioGateway, etc. em produção.
 */
@Component
@ConditionalOnProperty(
    name  = ["insights.engine.notification.dry-run"],
    havingValue = "true",
    matchIfMissing = true
)
class LogNotificationGateway : NotificationGateway {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(notification: Notification): Boolean {
        val icon = when (notification.channel) {
            NotificationChannel.EMAIL    -> "📧"
            NotificationChannel.SMS      -> "📱"
            NotificationChannel.PUSH     -> "🔔"
            NotificationChannel.WHATSAPP -> "💬"
        }
        log.info(
            "{} [{}] → recipient={} | priority={} | subject={} | meta={}",
            icon,
            notification.channel,
            notification.recipientId,
            notification.priority,
            notification.subject.ifBlank { "(sem assunto)" },
            notification.metadata
        )
        return true
    }
}
