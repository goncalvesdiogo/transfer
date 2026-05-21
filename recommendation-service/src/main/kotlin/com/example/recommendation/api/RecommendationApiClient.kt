package com.example.recommendation.api

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Simulates an external API client.
 * In a real scenario, this would be a RestTemplate / WebClient call.
 */
@Component
class RecommendationApiClient {

    fun getRecommendations(userId: String): List<RecommendationApiResponse> {
        // Simulated responses for demonstration
        return listOf(
            RecommendationApiResponse(
                useCase = "boleto_vence_amanha",
                commitments = listOf(
                    CommitmentApiDto("1", "Aluguel", BigDecimal("1500.00"), LocalDate.now().plusDays(1))
                )
            ),
            RecommendationApiResponse(
                useCase = "boleto_d0",
                commitments = listOf(
                    CommitmentApiDto("2", "Conta de luz", BigDecimal("200.00"), LocalDate.now()),
                    CommitmentApiDto("3", "Internet", BigDecimal("120.00"), LocalDate.now()),
                    CommitmentApiDto("4", "Agua", BigDecimal("80.00"), LocalDate.now())
                )
            ),
            RecommendationApiResponse(
                useCase = "boleto_d4",
                commitments = (1..15).map {
                    CommitmentApiDto("$it", "Compromisso $it", BigDecimal("100.00"), LocalDate.now().plusDays(4))
                }
            ),
            RecommendationApiResponse(
                useCase = "cartao_vence_hoje",
                commitments = listOf(
                    CommitmentApiDto("20", "Cartao Nubank", BigDecimal("3200.00"), LocalDate.now()),
                    CommitmentApiDto("21", "Cartao Inter", BigDecimal("1800.00"), LocalDate.now())
                )
            )
        )
    }
}
