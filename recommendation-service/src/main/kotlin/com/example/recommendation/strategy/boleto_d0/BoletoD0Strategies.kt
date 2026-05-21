package com.example.recommendation.strategy.boleto_d0

import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBatchStrategy
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import org.springframework.stereotype.Component

// Tiers: 1 | 2–10 | >10

@Component
class SingleBoletoD0Strategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "boleto_d0"
    override fun matches(count: Int) = count == 1
}

@Component
class BatchBoletoD0Strategy(loader: MessageTemplateLoader) : AbstractBatchStrategy(loader) {
    override val useCase = "boleto_d0"
    override fun matches(count: Int) = count in 2..10
}

@Component
class BulkBoletoD0Strategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "boleto_d0"
    override fun matches(count: Int) = count > 10
}
