package com.example.recommendation.strategy.boleto_vence_amanha

import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBatchStrategy
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import org.springframework.stereotype.Component

// Tiers: 1 | 2–10 | >10

@Component
class SingleBoletoVenceAmanhaStrategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count == 1
}

@Component
class BatchBoletoVenceAmanhaStrategy(loader: MessageTemplateLoader) : AbstractBatchStrategy(loader) {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count in 2..10
}

@Component
class BulkBoletoVenceAmanhaStrategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count > 10
}
