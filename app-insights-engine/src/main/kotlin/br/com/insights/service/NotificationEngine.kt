package br.com.insights.service

import br.com.insights.domain.ProcessingContext
import br.com.insights.domain.UseCaseResult
import br.com.insights.gateway.NotificationGateway
import br.com.insights.routing.DecisionTreeRouter
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Serviço principal do motor de insights.
 *
 * Orquestra a pipeline completa:
 *   1. Roteia o contexto para a árvore correta via [DecisionTreeRouter]
 *   2. Resolve e executa o use case
 *   3. Despacha as notificações via [NotificationGateway]
 *
 * Pattern: Facade + Service Layer
 */
@Service
class NotificationEngine(
    private val router: DecisionTreeRouter,
    private val gateway: NotificationGateway
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun process(context: ProcessingContext): UseCaseResult {
        val useCase = router.resolve(context)
            ?: return UseCaseResult.Skipped(
                useCaseName = "NONE",
                reason      = "Nenhuma regra para ${context.tipoCompromisso} " +
                              "id=${context.compromisso.id} D=${context.diasAteVencimento}"
            )

        log.info("Executando [{}] para compromisso [{}]", useCase.name, context.compromisso.id)

        val result = useCase.execute(context)

        if (result is UseCaseResult.Success) {
            val sent = gateway.sendAll(result.notifications)
            log.info("Despachadas {}/{} notificações — use case [{}]",
                sent, result.notifications.size, result.useCaseName)
        }

        return result
    }

    @Async
    fun processAsync(context: ProcessingContext): CompletableFuture<UseCaseResult> =
        CompletableFuture.completedFuture(process(context))

    fun processAll(contexts: List<ProcessingContext>): List<UseCaseResult> =
        contexts.map { process(it) }

    fun processBatch(contexts: List<ProcessingContext>): BatchResult {
        val results = processAll(contexts)
        return BatchResult(
            total    = results.size,
            success  = results.filterIsInstance<UseCaseResult.Success>().size,
            skipped  = results.filterIsInstance<UseCaseResult.Skipped>().size,
            failures = results.filterIsInstance<UseCaseResult.Failure>().size
        )
    }
}

data class BatchResult(
    val total: Int,
    val success: Int,
    val skipped: Int,
    val failures: Int
)
