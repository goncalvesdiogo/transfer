package com.example.recommendation.processor

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.factory.RecommendationStrategyFactory
import org.springframework.stereotype.Service

@Service
class RecommendationProcessor(
    private val factory: RecommendationStrategyFactory
) {
    fun process(request: RecommendationRequest): RecommendationResult {
        val strategy = factory.resolve(
            useCase = request.useCase,
            count   = request.commitments.size
        )
        return strategy.process(request)
    }
}
