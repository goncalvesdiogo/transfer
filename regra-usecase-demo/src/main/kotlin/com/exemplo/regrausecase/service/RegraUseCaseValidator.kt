package com.exemplo.regrausecase.service

import com.exemplo.regrausecase.dto.Condicao
import org.springframework.stereotype.Component

/**
 * Valida uma entidade (Boleto, Parcela, etc.) contra a lista de condicoes
 * de uma regra de caso de uso. Todas as condicoes precisam ser satisfeitas (AND).
 */
@Component
class RegraUseCaseValidator(
    private val funcaoResolver: FuncaoResolver,
    private val campoAccessor: CampoAccessor,
    private val valorCoercao: ValorCoercao,
    private val comparadorCondicao: ComparadorCondicao
) {

    fun validar(condicoes: List<Condicao>, entidade: Any, ativo: Boolean = true): ResultadoValidacao {
        if (!ativo) {
            return ResultadoValidacao(valido = false, motivo = "Regra inativa")
        }
        if (condicoes.isEmpty()) {
            return ResultadoValidacao(valido = false, motivo = "Regra sem condicoes definidas")
        }

        val falhas = mutableListOf<String>()

        condicoes.forEach { condicao ->
            try {
                val valorCampo = campoAccessor.obterValor(entidade, condicao.campo)
                val valorResolvido = funcaoResolver.resolver(condicao.valor)
                val valorEsperado = valorCampo?.let { valorCoercao.coagir(valorResolvido, it) } ?: valorResolvido

                val ok = comparadorCondicao.avaliar(valorCampo, condicao.operacao, valorEsperado)
                if (!ok) {
                    falhas += "Condicao '${condicao.campo} ${condicao.operacao} ${condicao.valor}' " +
                        "falhou (valor atual: $valorCampo)"
                }
            } catch (e: Exception) {
                falhas += "Erro ao avaliar '${condicao.campo}': ${e.message}"
            }
        }

        return ResultadoValidacao(
            valido = falhas.isEmpty(),
            motivo = if (falhas.isEmpty()) "Todas as condicoes satisfeitas" else falhas.joinToString("; ")
        )
    }
}
