package com.exemplo.regrausecase.service

import com.exemplo.regrausecase.dto.Condicao
import com.exemplo.regrausecase.model.Boleto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Teste unitario puro: instancia os colaboradores na mao, sem subir
 * o contexto Spring. Util para rodar rapido durante o desenvolvimento
 * do motor de regras em si.
 */
class RegraUseCaseValidatorUnitTest {

    private val dataFixa = LocalDate.of(2026, 7, 10)
    private val clock = Clock.fixed(dataFixa.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val validator = RegraUseCaseValidator(
        funcaoResolver = FuncaoResolver(clock),
        campoAccessor = CampoAccessor(),
        valorCoercao = ValorCoercao(),
        comparadorCondicao = ComparadorCondicao()
    )

    @Test
    fun `condicao unica dt_vcto maior ou igual a hoje deve ser valida`() {
        val condicoes = listOf(Condicao("dt_vcto", ">=", "\$hoje"))
        val boleto = Boleto("BOL-1", dataFixa, BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertTrue(resultado.valido)
    }

    @Test
    fun `multiplas condicoes com uma falhando deve invalidar e explicar o motivo`() {
        val condicoes = listOf(
            Condicao("dt_vcto", "==", "\$hoje"),
            Condicao("valor", ">", "1000")
        )
        val boleto = Boleto("BOL-2", dataFixa, BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertFalse(resultado.valido)
        assertTrue(resultado.motivo.contains("valor"))
    }

    @Test
    fun `campo inexistente na entidade deve gerar falha detalhada sem quebrar as demais condicoes`() {
        val condicoes = listOf(
            Condicao("campo_que_nao_existe", "==", "1"),
            Condicao("valor", ">", "0")
        )
        val boleto = Boleto("BOL-3", dataFixa, BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertFalse(resultado.valido)
        assertTrue(resultado.motivo.contains("campo_que_nao_existe"))
    }

    @Test
    fun `regra inativa deve ser invalida independente das condicoes`() {
        val condicoes = listOf(Condicao("dt_vcto", ">=", "\$hoje"))
        val boleto = Boleto("BOL-4", dataFixa, BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto, ativo = false)

        assertFalse(resultado.valido)
    }

    @Test
    fun `funcao hoje com argumento 3 (hoje mais 3 dias) deve resolver corretamente`() {
        val condicoes = listOf(Condicao("dt_vcto", "==", "\$hoje(3)"))
        val boleto = Boleto("BOL-5", dataFixa.plusDays(3), BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertTrue(resultado.valido, resultado.motivo)
    }

    @Test
    fun `funcao hoje com argumento negativo (hoje menos 1 dia) deve resolver corretamente`() {
        val condicoes = listOf(Condicao("dt_vcto", "==", "\$hoje(-1)"))
        val boleto = Boleto("BOL-6", dataFixa.minusDays(1), BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertTrue(resultado.valido, resultado.motivo)
    }

    @Test
    fun `funcao nao registrada deve gerar falha explicativa sem quebrar a validacao`() {
        val condicoes = listOf(Condicao("dt_vcto", "==", "\$funcaoQueNaoExiste"))
        val boleto = Boleto("BOL-7", dataFixa, BigDecimal("10.00"), "BOLETO")

        val resultado = validator.validar(condicoes, boleto)

        assertFalse(resultado.valido)
        assertTrue(resultado.motivo.contains("funcaoQueNaoExiste"))
    }

    @Test
    fun `deve permitir registrar uma nova funcao em tempo de execucao`() {
        val resolver = FuncaoResolver(clock)
        resolver.registrarFuncao("primeiroDiaDoMes") {
            dataFixa.withDayOfMonth(1)
        }
        val validatorComFuncaoCustom = RegraUseCaseValidator(
            funcaoResolver = resolver,
            campoAccessor = CampoAccessor(),
            valorCoercao = ValorCoercao(),
            comparadorCondicao = ComparadorCondicao()
        )

        val condicoes = listOf(Condicao("dt_vcto", "==", "\$primeiroDiaDoMes"))
        val boleto = Boleto("BOL-8", dataFixa.withDayOfMonth(1), BigDecimal("10.00"), "BOLETO")

        val resultado = validatorComFuncaoCustom.validar(condicoes, boleto)

        assertTrue(resultado.valido, resultado.motivo)
    }
}
