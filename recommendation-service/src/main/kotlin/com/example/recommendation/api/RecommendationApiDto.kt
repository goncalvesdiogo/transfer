package com.example.recommendation.api

import java.math.BigDecimal
import java.time.LocalDate

data class RecommendationApiResponse(
    val useCase: String,
    val commitments: List<CommitmentApiDto>
)

data class CommitmentApiDto(
    val id: String,
    val description: String,
    val amount: BigDecimal,
    val dueDate: LocalDate
)
