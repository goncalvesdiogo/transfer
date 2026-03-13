package br.com.insights.gateway

import br.com.insights.domain.Notification
import org.springframework.stereotype.Component

/**
 * Port de saída para envio de notificações.
 * Pattern: Port & Adapter (Hexagonal Architecture)
 */

interface NotificationGateway {
    fun send(notification: Notification): Boolean
    fun sendAll(notifications: List<Notification>): Int = notifications.count { send(it) }
}
