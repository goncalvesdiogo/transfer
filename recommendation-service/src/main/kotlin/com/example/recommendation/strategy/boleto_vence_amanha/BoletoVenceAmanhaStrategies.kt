package com.example.recommendation.strategy.boleto_vence_amanha

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.message.MessageTemplateLoader
import com.example.recommendation.strategy.AbstractBulkStrategy
import com.example.recommendation.strategy.AbstractSingleStrategy
import com.example.recommendation.strategy.RecommendationStrategy
import org.springframework.stereotype.Component

// Tiers: 1 | 2–10 | >10

@Component
class SingleBoletoVenceAmanhaStrategy(loader: MessageTemplateLoader) : AbstractSingleStrategy(loader) {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count == 1
}

@Component
class BatchBoletoVenceAmanhaStrategy(
    private val loader: MessageTemplateLoader
) : RecommendationStrategy {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count in 2..10

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
class BulkBoletoVenceAmanhaStrategy(loader: MessageTemplateLoader) : AbstractBulkStrategy(loader) {
    override val useCase = "boleto_vence_amanha"
    override fun matches(count: Int) = count > 10
}
