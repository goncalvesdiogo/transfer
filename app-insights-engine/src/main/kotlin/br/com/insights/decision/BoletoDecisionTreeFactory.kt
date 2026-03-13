package br.com.insights.decision

import br.com.insights.domain.StatusCompromisso
import br.com.insights.usecase.boleto.BoletoDueInFourDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueInTenDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueTodayUseCase
import org.springframework.stereotype.Component

/**
 * Factory Spring-managed da árvore de decisão para boletos.
 * Recebe os use cases por injeção de dependência.
 *
 * Regras: D-10 (email) | D-4 (email+SMS) | D0 (email+SMS+WhatsApp)
 */
@Component
class BoletoDecisionTreeFactory(
    private val d0: BoletoDueTodayUseCase,
    private val d4: BoletoDueInFourDaysUseCase,
    private val d10: BoletoDueInTenDaysUseCase
) {
    fun create(): DecisionTree = decisionTree("Boleto — Árvore de Notificações") {
        root("Boleto PENDENTE?", { it.compromisso.status == StatusCompromisso.PENDENTE }) {
            onTrue = condition("Vence em 10 dias?", { it.diasAteVencimento == 10L }) {
                onTrue  = useCase("D-10", d10)
                onFalse = condition("Vence em 4 dias?", { it.diasAteVencimento == 4L }) {
                    onTrue  = useCase("D-4", d4)
                    onFalse = condition("Vence hoje?", { it.diasAteVencimento == 0L }) {
                        onTrue  = useCase("D0", d0)
                        onFalse = noMatch("Sem regra de prazo para boleto")
                    }
                }
            }
            onFalse = noMatch("Boleto não PENDENTE")
        }
    }
}
