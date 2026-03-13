package br.com.insights.routing

import br.com.insights.decision.DecisionTree
import br.com.insights.decision.BoletoDecisionTreeFactory
import br.com.insights.decision.ParcelaDecisionTreeFactory
import br.com.insights.domain.ProcessingContext
import br.com.insights.domain.TipoCompromisso
import br.com.insights.usecase.NotificationUseCase
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Roteador central — recebe qualquer [ProcessingContext] e despacha
 * para a [DecisionTree] do tipo correspondente.
 *
 * Pattern: Registry + Chain of Responsibility
 *
 * Para adicionar um novo tipo: criar a Factory, injetá-la aqui e
 * registrá-la no [init].
 */
@Component
class DecisionTreeRouter(
    private val boletoFactory: BoletoDecisionTreeFactory,
    private val parcelaFactory: ParcelaDecisionTreeFactory
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val registry = mutableMapOf<TipoCompromisso, DecisionTree>()

    @PostConstruct
    fun init() {
        registry[TipoCompromisso.BOLETO]               = boletoFactory.create()
        registry[TipoCompromisso.PARCELA_FINANCIAMENTO] = parcelaFactory.create()
        log.info("DecisionTreeRouter inicializado com tipos: {}", registry.keys)
    }

    fun resolve(context: ProcessingContext): NotificationUseCase? {
        val tree = registry[context.tipoCompromisso]
        if (tree == null) {
            log.warn("Nenhuma árvore registrada para tipo [{}]", context.tipoCompromisso)
            return null
        }
        return tree.resolve(context)
    }

    fun registeredTypes(): Set<TipoCompromisso> = registry.keys.toSet()
}
