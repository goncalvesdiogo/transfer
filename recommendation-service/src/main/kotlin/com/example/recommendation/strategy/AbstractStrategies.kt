package com.example.recommendation.strategy

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.message.MessageTemplateLoader

/**
 * Base for single-commitment recommendations.
 * Loads the template and returns the message as-is — no placeholder substitution needed.
 */
abstract class AbstractSingleStrategy(
    private val messageLoader: MessageTemplateLoader
) : RecommendationStrategy {

    override fun process(request: RecommendationRequest): RecommendationResult {
        val message = messageLoader.load("$useCase.single")
        return RecommendationResult(
            useCase     = useCase,
            message     = message,
            templateKey = "$useCase.single"
        )
    }
}

/**
 * Base for bulk recommendations (high-count range).
 * Sets requiresAlternativeFlow = true to signal a different handling path.
 * Each use case may override process() to add extra metadata.
 */
abstract class AbstractBulkStrategy(
    private val messageLoader: MessageTemplateLoader
) : RecommendationStrategy {

    override fun process(request: RecommendationRequest): RecommendationResult {
        val total   = request.commitments.sumOf { it.amount }
        val message = messageLoader.load("$useCase.bulk")
            .replace("{{valor}}", total.toPlainString())

        return RecommendationResult(
            useCase                 = useCase,
            message                 = message,
            templateKey             = "$useCase.bulk",
            metadata                = mapOf("totalAmount" to total),
            requiresAlternativeFlow = true
        )
    }
}

// No AbstractBatchStrategy — batch logic is implemented directly in each strategy,
// since field substitution tends to vary per use case.
