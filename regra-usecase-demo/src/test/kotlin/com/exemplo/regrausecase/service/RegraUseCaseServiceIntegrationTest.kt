package com.exemplo.regrausecase.service

import com.exemplo.regrausecase.dto.Condicao
import com.exemplo.regrausecase.dto.RegraUseCasePayload
import com.exemplo.regrausecase.model.Boleto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Testes de integracao: sobem o contexto Spring completo (incluindo H2 em memoria),
 * salvam a regra via service (equivalente a persistir o payload real) e depois
 * buscam do banco para validar as condicoes contra um Boleto de teste.
 *
 * O Clock e fixado em 10/07/2026 para que "$hoje" seja deterministico nos testes.
 */
@SpringBootTest
@ActiveProfiles("test")
class RegraUseCaseServiceIntegrationTest {

    companion object {
        val DATA_FIXA: LocalDate = LocalDate.of(2026, 7, 10)
    }

    @TestConfiguration
    class ClockTestConfig {
        @Bean
        @Primary
        fun clockFixo(): Clock =
            Clock.fixed(
                DATA_FIXA.atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC
            )
    }

    @Autowired
    private lateinit var service: RegraUseCaseService

    @BeforeEach
    fun limparBancoLogico() {
        // Cada teste usa um idCasoUso diferente para nao colidir; H2 em memoria
        // e recriado a cada execucao do Maven, mas dentro da mesma JVM os
        // testes compartilham o schema, entao evitamos IDs fixos repetidos.
    }

    private fun payloadBoletoD0(idCasoUso: String) = RegraUseCasePayload(
        idCasoUso = idCasoUso,
        aplicavelA = listOf("BOLETO", "PARCELA"),
        usecaseName = "BOLETO_D0",
        ativo = true,
        condicoes = listOf(
            Condicao(campo = "dt_vcto", operacao = ">=", valor = "\$hoje")
        ),
        modoDisparo = "INDIVIDUAL"
    )

    private fun payloadBoletoDMais3(idCasoUso: String) = RegraUseCasePayload(
        idCasoUso = idCasoUso,
        aplicavelA = listOf("BOLETO"),
        usecaseName = "BOLETO_D_MAIS_3",
        ativo = true,
        condicoes = listOf(
            Condicao(campo = "dt_vcto", operacao = "==", valor = "\$hoje(3)"),
            Condicao(campo = "valor", operacao = ">", valor = "0")
        ),
        modoDisparo = "INDIVIDUAL"
    )

    @Test
    fun `deve salvar regra e buscar de volta do banco H2`() {
        val payload = payloadBoletoD0("TESTE-001")

        val salva = service.salvar(payload)
        assertNotNull(salva)

        val buscada = service.buscarPorId("TESTE-001")
        assertNotNull(buscada)
        assertEquals("BOLETO_D0", buscada!!.usecaseName)
        assertEquals(1, buscada.condicoes.size)
        assertEquals(listOf("BOLETO", "PARCELA"), buscada.aplicavelA)
    }

    @Test
    fun `deve validar boleto com vencimento igual a hoje contra regra D0 buscada do banco`() {
        service.salvar(payloadBoletoD0("TESTE-002"))

        val boleto = Boleto(
            id = "BOL-1",
            dtVcto = DATA_FIXA,
            valor = BigDecimal("150.00"),
            tipo = "BOLETO"
        )

        val resultado = service.validarEntidadeContraRegra("TESTE-002", boleto)

        assertTrue(resultado.valido, "Esperava valido=true, motivo: ${resultado.motivo}")
    }

    @Test
    fun `deve invalidar boleto com vencimento no passado contra regra D0`() {
        service.salvar(payloadBoletoD0("TESTE-003"))

        val boleto = Boleto(
            id = "BOL-2",
            dtVcto = DATA_FIXA.minusDays(1),
            valor = BigDecimal("150.00"),
            tipo = "BOLETO"
        )

        val resultado = service.validarEntidadeContraRegra("TESTE-003", boleto)

        assertFalse(resultado.valido)
        assertTrue(resultado.motivo.contains("dt_vcto"))
    }

    @Test
    fun `deve validar regra com multiplas condicoes (D+3 e valor maior que zero)`() {
        service.salvar(payloadBoletoDMais3("TESTE-004"))

        val boletoValido = Boleto(
            id = "BOL-3",
            dtVcto = DATA_FIXA.plusDays(3),
            valor = BigDecimal("50.00"),
            tipo = "BOLETO"
        )
        val resultadoValido = service.validarEntidadeContraRegra("TESTE-004", boletoValido)
        assertTrue(resultadoValido.valido, "Esperava valido=true, motivo: ${resultadoValido.motivo}")

        val boletoValorZero = Boleto(
            id = "BOL-4",
            dtVcto = DATA_FIXA.plusDays(3),
            valor = BigDecimal.ZERO,
            tipo = "BOLETO"
        )
        val resultadoInvalido = service.validarEntidadeContraRegra("TESTE-004", boletoValorZero)
        assertFalse(resultadoInvalido.valido)
        assertTrue(resultadoInvalido.motivo.contains("valor"))
    }

    @Test
    fun `deve retornar invalido quando regra nao existe no banco`() {
        val boleto = Boleto(
            id = "BOL-5",
            dtVcto = DATA_FIXA,
            valor = BigDecimal.TEN,
            tipo = "BOLETO"
        )

        val resultado = service.validarEntidadeContraRegra("ID-INEXISTENTE", boleto)

        assertFalse(resultado.valido)
        assertTrue(resultado.motivo.contains("nao encontrada"))
    }

    @Test
    fun `deve invalidar quando regra esta inativa`() {
        val payload = payloadBoletoD0("TESTE-006").copy(ativo = false)
        service.salvar(payload)

        val boleto = Boleto(
            id = "BOL-6",
            dtVcto = DATA_FIXA,
            valor = BigDecimal.TEN,
            tipo = "BOLETO"
        )

        val resultado = service.validarEntidadeContraRegra("TESTE-006", boleto)

        assertFalse(resultado.valido)
        assertEquals("Regra inativa", resultado.motivo)
    }
}
