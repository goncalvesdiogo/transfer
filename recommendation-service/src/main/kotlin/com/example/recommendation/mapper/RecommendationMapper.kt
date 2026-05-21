package com.example.recommendation.mapper

import com.example.recommendation.api.RecommendationApiResponse
import com.example.recommendation.domain.FinancialCommitment
import com.example.recommendation.domain.RecommendationRequest
import org.springframework.stereotype.Component

@Component
class RecommendationMapper {

    fun toDomain(response: RecommendationApiResponse): RecommendationRequest =
        RecommendationRequest(
            useCase = response.useCase,
            commitments = response.commitments.map {
                FinancialCommitment(
                    id          = it.id,
                    description = it.description,
                    amount      = it.amount,
                    dueDate     = it.dueDate
                )
            }
        )
}
