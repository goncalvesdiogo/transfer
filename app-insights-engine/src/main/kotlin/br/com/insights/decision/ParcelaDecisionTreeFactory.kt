package br.com.insights.decision

import br.com.insights.domain.StatusCompromisso
import br.com.insights.usecase.parcela.ParcelaDueInSevenDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueInThreeDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueTodayUseCase
import org.springframework.stereotype.Component

/**
 * Factory Spring-managed da árvore de decisão para parcelas de financiamento.
 * Regras independentes do boleto: D-7 | D-3 | D0
 * Também processa status RENEGOCIADO.
 */
@Component
class ParcelaDecisionTreeFactory(
    private val d0: ParcelaDueTodayUseCase,
    private val d3: ParcelaDueInThreeDaysUseCase,
    private val d7: ParcelaDueInSevenDaysUseCase
) {
    fun create(): DecisionTree = decisionTree("Parcela — Árvore de Notificações") {
        root(
            "Parcela ativa (PENDENTE ou RENEGOCIADA)?",
            { it.compromisso.status in setOf(StatusCompromisso.PENDENTE, StatusCompromisso.RENEGOCIADO) }
        ) {
            onTrue = condition("Vence em 7 dias?", { it.diasAteVencimento == 7L }) {
                onTrue  = useCase("D-7", d7)
                onFalse = condition("Vence em 3 dias?", { it.diasAteVencimento == 3L }) {
                    onTrue  = useCase("D-3", d3)
                    onFalse = condition("Vence hoje?", { it.diasAteVencimento == 0L }) {
                        onTrue  = useCase("D0", d0)
                        onFalse = noMatch("Sem regra de prazo para parcela")
                    }
                }
            }
            onFalse = noMatch("Parcela não está em estado notificável")
        }
    }
}
