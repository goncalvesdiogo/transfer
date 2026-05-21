package com.example.recommendation.factory

import com.example.recommendation.strategy.RecommendationStrategy
import org.springframework.stereotype.Component

@Component
class RecommendationStrategyFactory(
    strategies: List<RecommendationStrategy>
) {
    /**
     * Groups all strategies by useCase at startup.
     * Spring injects every @Component implementing RecommendationStrategy automatically.
     */
    private val registry: Map<String, List<RecommendationStrategy>> =
        strategies.groupBy { it.useCase }

    fun resolve(useCase: String, count: Int): RecommendationStrategy {
        val candidates = registry[useCase]
            ?: throw IllegalArgumentException(
                "UseCase não registrado: '$useCase'. " +
                "Casos registrados: ${registry.keys}"
            )

        return candidates.firstOrNull { it.matches(count) }
            ?: throw IllegalArgumentException(
                "Nenhuma strategy para useCase='$useCase' com count=$count. " +
                "Verifique as regras de matches() das strategies registradas."
            )
    }
}
