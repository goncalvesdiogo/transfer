package br.com.insights.infra.persistence

import br.com.insights.domain.ProcessingContext
import br.com.insights.domain.UseCaseResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuditService(
    private val repository: ProcessingRecordRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun record(context: ProcessingContext, result: UseCaseResult) {
        val (useCaseName, status, message, dispatched) = when (result) {
            is UseCaseResult.Success -> Quadruple(result.useCaseName, "SUCCESS",  result.message, result.notifications.size)
            is UseCaseResult.Skipped -> Quadruple(result.useCaseName, "SKIPPED",  result.reason, 0)
            is UseCaseResult.Failure -> Quadruple(result.useCaseName, "FAILURE",  result.message, 0)
        }

        val record = ProcessingRecord(
            compromissoId          = context.compromisso.id,
            tipoCompromisso        = context.tipoCompromisso.name,
            useCaseName            = useCaseName,
            resultStatus           = status,
            resultMessage          = message,
            notificationsDispatched = dispatched
        )

        runCatching { repository.save(record) }
            .onFailure { log.error("Falha ao salvar registro de auditoria: {}", it.message) }
    }

    private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
