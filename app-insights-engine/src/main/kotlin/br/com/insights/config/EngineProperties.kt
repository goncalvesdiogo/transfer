package br.com.insights.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Propriedades tipadas do motor — mapeadas de application.yml.
 * Acesse via injeção de dependência em qualquer bean Spring.
 */
@ConfigurationProperties(prefix = "insights.engine")
data class EngineProperties(
    val asyncProcessing: Boolean = true,
    val maxBatchSize: Int = 500,
    val notification: NotificationProperties = NotificationProperties()
) {
    data class NotificationProperties(
        val dryRun: Boolean = false,
        val channels: ChannelsProperties = ChannelsProperties()
    )

    data class ChannelsProperties(
        val email: Boolean = true,
        val sms: Boolean = true,
        val whatsapp: Boolean = true
    )
}
