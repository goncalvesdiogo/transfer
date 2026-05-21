package com.example.recommendation.strategy.cartao_vence_hoje

import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBatchStrategy
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import org.springframework.stereotype.Component

// Different tier boundaries from boleto use cases:
// Tiers: 1–3 | 4–20 | >20

@Component
class SingleCartaoVenceHojeStrategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count in 1..3
}

@Component
class BatchCartaoVenceHojeStrategy(loader: MessageTemplateLoader) : AbstractBatchStrategy(loader) {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count in 4..20
}

@Component
class BulkCartaoVenceHojeStrategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count > 20
}
