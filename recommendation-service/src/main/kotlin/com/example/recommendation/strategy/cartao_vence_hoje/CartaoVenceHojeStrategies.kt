package com.example.recommendation.strategy.cartao_vence_hoje

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import com.example.recommendation.strategy.RecommendationStrategy
import org.springframework.stereotype.Component

// Different tier boundaries from boleto use cases:
// Tiers: 1–3 | 4–20 | >20

@Component
class SingleCartaoVenceHojeStrategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count in 1..3
}

@Component
class BatchCartaoVenceHojeStrategy(
    private val loader: MessageTemplateLoader
) : RecommendationStrategy {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count in 4..20

    override fun process(request: RecommendationRequest): RecommendationResult {
        val count = request.commitments.size
        val total = request.commitments.sumOf { it.amount }
        val message = loader.load("$useCase.batch")
            .replace("{{quantidade}}", count.toString())
            .replace("{{valor}}", total.toPlainString())

        return RecommendationResult(
            useCase     = useCase,
            message     = message,
            templateKey = "$useCase.batch",
            metadata    = mapOf("count" to count, "totalAmount" to total)
        )
    }
}

@Component
class BulkCartaoVenceHojeStrategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "cartao_vence_hoje"
    override fun matches(count: Int) = count > 20
}
