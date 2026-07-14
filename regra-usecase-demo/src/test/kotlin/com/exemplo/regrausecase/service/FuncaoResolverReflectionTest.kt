package com.exemplo.regrausecase.service

import com.exemplo.regrausecase.dominio.FuncoesDominio
import com.exemplo.regrausecase.dto.Condicao
import com.exemplo.regrausecase.model.Boleto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Testa o registro de funcoes JA EXISTENTES no codigo (nao lambdas escritas
 * so para o motor de regras), via referencia de funcao + reflection.
 */
class FuncaoResolverReflectionTest {

    // 2026-07-10 e uma sexta-feira
    private val sexta = LocalDate.of(2026, 7, 10)
    private val clock = Clock.fixed(sexta.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val resolver = FuncaoResolver(clock).apply {
        registrarFuncaoExistente("proximoDiaUtil", FuncoesDominio::proximoDiaUtil)
        registrarFuncaoExistente("valorComDesconto", FuncoesDominio::valorComDesconto)
    }

    @Test
    fun `deve chamar funcao existente com todos os argumentos informados`() {
        // proximo dia util a partir de sexta, pulando fim de semana -> segunda 2026-07-13
        val resultado = resolver.resolver("\$proximoDiaUtil(2026-07-10, true)")
        assertTrue(resultado == LocalDate.of(2026, 7, 13))
    }

    @Test
    fun `deve usar o valor default do parametro quando nao informado na condicao`() {
        // pularSabadoDomingo tem default=true, entao omitir deve dar o mesmo resultado
        val resultado = resolver.resolver("\$proximoDiaUtil(2026-07-10)")
        assertTrue(resultado == LocalDate.of(2026, 7, 13))
    }

    @Test
    fun `deve respeitar argumento explicito que sobrescreve o default`() {
        // sem pular fim de semana -> sabado 2026-07-11
        val resultado = resolver.resolver("\$proximoDiaUtil(2026-07-10, false)")
        assertTrue(resultado == LocalDate.of(2026, 7, 11))
    }

    @Test
    fun `deve converter BigDecimal e Double corretamente`() {
        val resultado = resolver.resolver("\$valorComDesconto(200.00, 10)") as BigDecimal
        assertTrue(resultado.compareTo(BigDecimal("180.0")) == 0)
    }

    @Test
    fun `deve usar valor da funcao existente diretamente numa condicao do validador`() {
        val validator = RegraUseCaseValidator(
            funcaoResolver = resolver,
            campoAccessor = CampoAccessor(),
            valorCoercao = ValorCoercao(),
            comparadorCondicao = ComparadorCondicao()
        )

        val condicoes = listOf(
            Condicao(campo = "dt_vcto", operacao = "==", valor = "\$proximoDiaUtil(2026-07-10)")
        )
        val boleto = Boleto("BOL-1", LocalDate.of(2026, 7, 13), BigDecimal("100.00"), "BOLETO")

        val resultadoValidacao = validator.validar(condicoes, boleto)

        assertTrue(resultadoValidacao.valido, resultadoValidacao.motivo)
    }

    @Test
    fun `deve lancar erro claro quando argumentos excedem o esperado pela funcao`() {
        val excecao = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolver("\$proximoDiaUtil(2026-07-10, true, true)")
        }
        assertTrue(excecao.message!!.contains("maximo"))
    }

    @Test
    fun `deve lancar erro claro quando o tipo do argumento e invalido`() {
        assertThrows(Exception::class.java) {
            resolver.resolver("\$proximoDiaUtil(nao-e-uma-data)")
        }
    }
}
