package com.example.recommendation

import com.example.recommendation.api.RecommendationApiClient
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.mapper.RecommendationMapper
import com.example.recommendation.processor.RecommendationProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val apiClient: RecommendationApiClient,
    private val mapper: RecommendationMapper,
    private val processor: RecommendationProcessor
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchAndProcess(userId: String): List<RecommendationResult> {
        log.info("Buscando recomendações para userId={}", userId)

        return apiClient.getRecommendations(userId)
            .map { mapper.toDomain(it) }
            .map { request ->
                log.info(
                    "Processando useCase={} com {} compromisso(s)",
                    request.useCase,
                    request.commitments.size
                )
                processor.process(request)
            }
    }
}
