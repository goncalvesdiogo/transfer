package com.example.recommendation.strategy

import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult

interface RecommendationStrategy {

    val useCase: String

    /**
     * Each strategy declares its own quantity rule.
     * This allows different use cases to have completely independent tier boundaries.
     */
    fun matches(count: Int): Boolean

    fun process(request: RecommendationRequest): RecommendationResult
}
