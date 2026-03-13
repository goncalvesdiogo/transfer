package br.com.insights.decision

import br.com.insights.domain.ProcessingContext
import br.com.insights.usecase.NotificationUseCase

// ── Interface ─────────────────────────────────────────────────────────────────

/**
 * Nó da árvore de decisão.
 * Pattern: Composite + Chain of Responsibility
 */
interface DecisionNode {
    val label: String
    fun evaluate(context: ProcessingContext): NotificationUseCase?
}

// ── Implementações ────────────────────────────────────────────────────────────

class ConditionNode(
    override val label: String,
    private val condition: (ProcessingContext) -> Boolean,
    private val onTrue: DecisionNode,
    private val onFalse: DecisionNode? = null
) : DecisionNode {
    override fun evaluate(ctx: ProcessingContext) =
        if (condition(ctx)) onTrue.evaluate(ctx) else onFalse?.evaluate(ctx)
}

class UseCaseNode(
    override val label: String,
    private val useCase: NotificationUseCase
) : DecisionNode {
    override fun evaluate(ctx: ProcessingContext) = useCase
}

class NoMatchNode(override val label: String = "NO_MATCH") : DecisionNode {
    override fun evaluate(ctx: ProcessingContext): NotificationUseCase? = null
}

// ── DecisionTree ──────────────────────────────────────────────────────────────

class DecisionTree(
    val name: String,
    private val root: DecisionNode
) {
    fun resolve(context: ProcessingContext): NotificationUseCase? = root.evaluate(context)
}

// ── DSL Builder ───────────────────────────────────────────────────────────────

class ConditionNodeBuilder(
    private val label: String,
    private val condition: (ProcessingContext) -> Boolean
) {
    var onTrue: DecisionNode = NoMatchNode("TRUE_NOT_SET")
    var onFalse: DecisionNode? = null

    fun build() = ConditionNode(label, condition, onTrue, onFalse)
}

fun decisionTree(name: String, block: DecisionTreeBuilder.() -> Unit): DecisionTree =
    DecisionTreeBuilder(name).apply(block).build()

class DecisionTreeBuilder(private val name: String) {
    private var root: DecisionNode? = null

    fun root(label: String, condition: (ProcessingContext) -> Boolean, block: ConditionNodeBuilder.() -> Unit) {
        root = ConditionNodeBuilder(label, condition).apply(block).build()
    }

    fun build() = DecisionTree(name, requireNotNull(root) { "Árvore [$name] sem nó raiz" })
}

fun condition(label: String, condition: (ProcessingContext) -> Boolean, block: ConditionNodeBuilder.() -> Unit): DecisionNode =
    ConditionNodeBuilder(label, condition).apply(block).build()

fun useCase(label: String, useCase: NotificationUseCase): DecisionNode = UseCaseNode(label, useCase)
fun noMatch(label: String = "NO_MATCH"): DecisionNode = NoMatchNode(label)
