package com.example.recommendation.strategy.boleto_d4

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBatchStrategy
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import org.springframework.stereotype.Component

// Tiers: 1 | 2–10 | >10
// Bulk override: adds urgencyLevel to metadata

@Component
class SingleBoletoD4Strategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "boleto_d4"
    override fun matches(count: Int) = count == 1
}

@Component
class BatchBoletoD4Strategy(loader: MessageTemplateLoader) : AbstractBatchStrategy(loader) {
    override val useCase = "boleto_d4"
    override fun matches(count: Int) = count in 2..10
}

@Component
class BulkBoletoD4Strategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "boleto_d4"
    override fun matches(count: Int) = count > 10

    // boleto_d4 bulk requires extra metadata — only overrides what changes
    override fun process(request: RecommendationRequest): RecommendationResult {
        val base = super.process(request)
        return base.copy(
            metadata = base.metadata + mapOf("urgencyLevel" to "HIGH")
        )
    }
}
