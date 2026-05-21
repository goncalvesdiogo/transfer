package com.example.recommendation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RecommendationServiceIntegrationTest {

    @Autowired
    lateinit var service: RecommendationService

    @Test
    fun `full flow returns results for all use cases`() {
        val results = service.fetchAndProcess("user-123")
        assertEquals(4, results.size)
    }

    @Test
    fun `boleto_vence_amanha with 1 commitment uses single template`() {
        val results = service.fetchAndProcess("user-123")
        val result = results.first { it.useCase == "boleto_vence_amanha" }
        assertEquals("boleto_vence_amanha.single", result.templateKey)
        assertTrue(result.message.isNotBlank())
    }

    @Test
    fun `boleto_d0 with 3 commitments uses batch template and injects values`() {
        val results = service.fetchAndProcess("user-123")
        val result = results.first { it.useCase == "boleto_d0" }
        assertEquals("boleto_d0.batch", result.templateKey)
        assertTrue(result.message.contains("3"))   // quantidade
        assertTrue(result.message.contains("400")) // 200 + 120 + 80
    }

    @Test
    fun `boleto_d4 with 15 commitments uses bulk template and has alternative flow`() {
        val results = service.fetchAndProcess("user-123")
        val result = results.first { it.useCase == "boleto_d4" }
        assertEquals("boleto_d4.bulk", result.templateKey)
        assertTrue(result.requiresAlternativeFlow)
        assertEquals("HIGH", result.metadata["urgencyLevel"])
    }

    @Test
    fun `cartao_vence_hoje with 2 commitments falls in single tier (1 to 3)`() {
        val results = service.fetchAndProcess("user-123")
        val result = results.first { it.useCase == "cartao_vence_hoje" }
        assertEquals("cartao_vence_hoje.single", result.templateKey)
    }
}
