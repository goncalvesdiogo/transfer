package br.com.insights.domain

data class Notification(
    val recipientId: String,
    val recipientEmail: String,
    val recipientPhone: String,
    val subject: String,
    val message: String,
    val channel: NotificationChannel,
    val priority: NotificationPriority,
    val metadata: Map<String, Any> = emptyMap()
)

enum class NotificationChannel { EMAIL, SMS, PUSH, WHATSAPP }
enum class NotificationPriority { LOW, MEDIUM, HIGH, CRITICAL }
