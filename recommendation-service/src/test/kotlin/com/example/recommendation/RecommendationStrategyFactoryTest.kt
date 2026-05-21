package com.example.recommendation.factory

import com.example.recommendation.domain.FinancialCommitment
import com.example.recommendation.domain.RecommendationRequest
import com.example.recommendation.domain.RecommendationResult
import com.example.recommendation.strategy.RecommendationStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class RecommendationStrategyFactoryTest {

    private fun makeStrategy(uc: String, matchFn: (Int) -> Boolean) =
        object : RecommendationStrategy {
            override val useCase = uc
            override fun matches(count: Int) = matchFn(count)
            override fun process(request: RecommendationRequest) =
                RecommendationResult(uc, "msg", "key")
        }

    private val strategies = listOf(
        makeStrategy("boleto_d0") { it == 1 },
        makeStrategy("boleto_d0") { it in 2..10 },
        makeStrategy("boleto_d0") { it > 10 },
        makeStrategy("cartao_vence_hoje") { it in 1..3 },
        makeStrategy("cartao_vence_hoje") { it in 4..20 },
        makeStrategy("cartao_vence_hoje") { it > 20 }
    )

    private val factory = RecommendationStrategyFactory(strategies)

    @Test
    fun `resolve single boleto_d0`() {
        val strategy = factory.resolve("boleto_d0", 1)
        assertEquals("boleto_d0", strategy.useCase)
        assert(strategy.matches(1))
    }

    @Test
    fun `resolve batch boleto_d0`() {
        val strategy = factory.resolve("boleto_d0", 5)
        assert(strategy.matches(5))
        assert(!strategy.matches(1))
    }

    @Test
    fun `resolve bulk boleto_d0`() {
        val strategy = factory.resolve("boleto_d0", 15)
        assert(strategy.matches(15))
    }

    @Test
    fun `cartao uses different tier boundaries - 2 falls in single tier`() {
        val strategy = factory.resolve("cartao_vence_hoje", 2)
        assert(strategy.matches(2))   // 1..3 = single tier for cartao
        assert(strategy.matches(3))
        assert(!strategy.matches(4))
    }

    @Test
    fun `unregistered useCase throws exception`() {
        assertThrows<IllegalArgumentException> {
            factory.resolve("pix_agendado", 1)
        }
    }

    @Test
    fun `no matching count throws exception`() {
        // boleto_d0 has no strategy for count = 0
        assertThrows<IllegalArgumentException> {
            factory.resolve("boleto_d0", 0)
        }
    }
}
