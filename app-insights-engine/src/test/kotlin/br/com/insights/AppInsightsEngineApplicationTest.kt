package br.com.insights

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class AppInsightsEngineApplicationTest {

    @Test
    fun `context loads`() {
        // Verifica que o contexto Spring sobe sem erros
    }
}
