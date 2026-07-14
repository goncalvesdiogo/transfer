package com.exemplo.regrausecase.dominio

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Funcoes de dominio "normais", que já existiriam no seu codigo de qualquer forma
 * (nada aqui e especifico do motor de regras). A ideia e mostrar que voce pode
 * registrar qualquer uma delas no FuncaoResolver via referencia de funcao,
 * sem precisar reescrever a logica como lambda.
 */
object FuncoesDominio {

    /**
     * Proximo dia util a partir de uma data base.
     * `pularFeriado` e um parametro com default, entao a condicao pode
     * omiti-lo: "$proximoDiaUtil(2026-07-10)".
     */
    fun proximoDiaUtil(dataBase: LocalDate, pularSabadoDomingo: Boolean = true): LocalDate {
        var data = dataBase.plusDays(1)
        if (pularSabadoDomingo) {
            while (data.dayOfWeek == DayOfWeek.SATURDAY || data.dayOfWeek == DayOfWeek.SUNDAY) {
                data = data.plusDays(1)
            }
        }
        return data
    }

    /**
     * Aplica um percentual de desconto sobre um valor base.
     */
    fun valorComDesconto(valorBase: BigDecimal, percentualDesconto: Double = 0.0): BigDecimal {
        val fator = BigDecimal.ONE.subtract(BigDecimal.valueOf(percentualDesconto / 100.0))
        return valorBase.multiply(fator)
    }
}
