package com.exemplo.regrausecase.service

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class ValorCoercao {

    fun coagir(valorResolvido: Any, referencia: Any): Any {
        if (valorResolvido::class == referencia::class) return valorResolvido
        return when (referencia) {
            is LocalDate -> LocalDate.parse(valorResolvido.toString())
            is BigDecimal -> BigDecimal(valorResolvido.toString())
            is Int -> valorResolvido.toString().toInt()
            is Long -> valorResolvido.toString().toLong()
            is Boolean -> valorResolvido.toString().toBoolean()
            else -> valorResolvido
        }
    }
}
