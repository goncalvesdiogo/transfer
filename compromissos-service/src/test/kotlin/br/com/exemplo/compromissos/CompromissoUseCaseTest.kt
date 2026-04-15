package br.com.exemplo.compromissos

import br.com.exemplo.compromissos.domain.Compromisso
import br.com.exemplo.compromissos.domain.Notificacao
import br.com.exemplo.compromissos.domain.UseCaseResult
import br.com.exemplo.compromissos.factory.NotificacaoFactory
import br.com.exemplo.compromissos.repository.NotificacaoRepository
import br.com.exemplo.compromissos.service.ProcessarCompromissoService
import br.com.exemplo.compromissos.service.RecuperarNotificacoesService
import br.com.exemplo.compromissos.usecase.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class CompromissoUseCaseTest {

    private fun compromisso(
        dataVencimento: LocalDate = LocalDate.now().plusDays(10),
        valor: BigDecimal = BigDecimal("100.00"),
        nomeBeneficiario: String = "João Silva",
        metadado: String = "meta"
    ) = Compromisso(
        idCompromisso    = UUID.randomUUID(),
        dataVencimento   = dataVencimento,
        nomeBeneficiario = nomeBeneficiario,
        valor            = valor,
        metadado         = metadado
    )

    @Nested
    inner class VencimentoFuturoUseCaseTest {
        private val useCase = VencimentoFuturoUseCase()

        @Test
        fun `deve ser elegivel quando data vencimento e futura`() {
            val result = useCase.isEligible(compromisso(dataVencimento = LocalDate.now().plusDays(1)))
            assertTrue(result.eligible)
        }

        @Test
        fun `nao deve ser elegivel quando data vencimento e passada`() {
            val result = useCase.isEligible(compromisso(dataVencimento = LocalDate.now().minusDays(1)))
            assertFalse(result.eligible)
            assertNotNull(result.reason)
        }
    }

    @Nested
    inner class ValorMinimoUseCaseTest {
        private val useCase = ValorMinimoUseCase()

        @Test
        fun `deve ser elegivel quando valor e positivo`() {
            val result = useCase.isEligible(compromisso(valor = BigDecimal("0.01")))
            assertTrue(result.eligible)
        }

        @Test
        fun `nao deve ser elegivel quando valor e zero`() {
            val result = useCase.isEligible(compromisso(valor = BigDecimal.ZERO))
            assertFalse(result.eligible)
        }
    }

    @Nested
    inner class BeneficiarioPreenchidoUseCaseTest {
        private val useCase = BeneficiarioPreenchidoUseCase()

        @Test
        fun `deve ser elegivel quando beneficiario esta preenchido`() {
            val result = useCase.isEligible(compromisso(nomeBeneficiario = "Maria"))
            assertTrue(result.eligible)
        }

        @Test
        fun `nao deve ser elegivel quando beneficiario esta em branco`() {
            val result = useCase.isEligible(compromisso(nomeBeneficiario = "   "))
            assertFalse(result.eligible)
        }
    }

    @Nested
    inner class UseCaseRegistryTest {
        private val registry = UseCaseRegistry(
            listOf(
                VencimentoFuturoUseCase(),
                ValorMinimoUseCase(),
                BeneficiarioPreenchidoUseCase(),
                MetadadoPresenteUseCase()
            )
        )

        @Test
        fun `deve retornar todos os usecases elegiveis para compromisso valido`() {
            val results = registry.eligibleOnly(compromisso())
            assertEquals(4, results.size)
            assertTrue(results.all { it.eligible })
        }

        @Test
        fun `deve retornar apenas usecases elegiveis quando compromisso parcialmente invalido`() {
            // Valor zero — UC-002 não deve ser elegível
            val results = registry.eligibleOnly(compromisso(valor = BigDecimal.ZERO))
            assertEquals(3, results.size)
            assertFalse(results.any { it.useCaseId == "UC-002-VALOR-MINIMO" })
        }
    }

    @Nested
    inner class ProcessarCompromissoServiceTest {
        private val registry    = mockk<UseCaseRegistry>()
        private val factory     = NotificacaoFactory()
        private val repository  = mockk<NotificacaoRepository>(relaxed = true)
        private val service     = ProcessarCompromissoService(registry, factory, repository)

        @Test
        fun `deve salvar notificacoes para cada usecase elegivel`() {
            val comp = compromisso()
            every { registry.eligibleOnly(comp) } returns listOf(
                UseCaseResult("UC-001", true),
                UseCaseResult("UC-002", true)
            )

            val notificacoes = service.processar(comp)

            assertEquals(2, notificacoes.size)
            verify(exactly = 1) { repository.salvarTodas(any()) }
        }

        @Test
        fun `deve retornar lista vazia quando nao ha usecases elegiveis`() {
            val comp = compromisso()
            every { registry.eligibleOnly(comp) } returns emptyList()

            val notificacoes = service.processar(comp)

            assertTrue(notificacoes.isEmpty())
            verify(exactly = 0) { repository.salvarTodas(any()) }
        }
    }

    @Nested
    inner class RecuperarNotificacoesServiceTest {
        private val registry   = UseCaseRegistry(
            listOf(VencimentoFuturoUseCase(), ValorMinimoUseCase())
        )
        private val repository = mockk<NotificacaoRepository>()
        private val service    = RecuperarNotificacoesService(registry, repository)

        private fun notificacao(
            useCaseId: String,
            dataVencimento: LocalDate = LocalDate.now().plusDays(10),
            valor: BigDecimal = BigDecimal("50.00")
        ) = Notificacao(
            idNotificacao    = UUID.randomUUID(),
            idCompromisso    = UUID.randomUUID(),
            dataVencimento   = dataVencimento,
            nomeBeneficiario = "Beneficiario",
            valor            = valor,
            metadado         = "meta",
            dataPublicacao   = LocalDate.now(),
            useCaseId        = useCaseId
        )

        @Test
        fun `deve retornar apenas notificacoes ainda elegiveis`() {
            val hoje = LocalDate.now()
            val notificacoes = listOf(
                notificacao("UC-001-VENCIMENTO-FUTURO"),                        // ainda elegível
                notificacao("UC-001-VENCIMENTO-FUTURO", dataVencimento = hoje.minusDays(1)), // não mais elegível
                notificacao("UC-002-VALOR-MINIMO")                             // elegível
            )
            every { repository.buscarPorDataPublicacao(hoje) } returns notificacoes

            val elegiveis = service.recuperarElegiveis(hoje)

            assertEquals(2, elegiveis.size)
        }
    }
}
