package com.example.recommendation.domain

import java.math.BigDecimal
import java.time.LocalDate

data class RecommendationRequest(
    val useCase: String,
    val commitments: List<FinancialCommitment>
)

data class FinancialCommitment(
    val id: String,
    val description: String,
    val amount: BigDecimal,
    val dueDate: LocalDate
)

data class RecommendationResult(
    val useCase: String,
    val message: String,
    val templateKey: String,
    val metadata: Map<String, Any> = emptyMap(),
    val requiresAlternativeFlow: Boolean = false
)
