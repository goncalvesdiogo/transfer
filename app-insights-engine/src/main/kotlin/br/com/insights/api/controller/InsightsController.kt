package br.com.insights.api.controller

import br.com.insights.service.NotificationEngine
import br.com.insights.api.dto.*
import br.com.insights.domain.ProcessingContext
import br.com.insights.domain.UseCaseResult
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * Endpoints REST do motor de insights.
 *
 * POST /insights/process         — processa um compromisso
 * POST /insights/process/batch   — processa um lote de compromissos
 */
@RestController
@RequestMapping("/insights")
class InsightsController(
    private val engine: NotificationEngine
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/process")
    fun process(
        @Valid @RequestBody request: ProcessRequest,
        @RequestParam(defaultValue = "") referenceDate: String
    ): ResponseEntity<ProcessResponse> {
        val refDate = referenceDate.ifBlank { null }?.let { LocalDate.parse(it) } ?: LocalDate.now()
        val context = ProcessingContext(
            compromisso    = request.toCompromisso(),
            dataReferencia = refDate
        )

        log.info("POST /insights/process | tipo={} id={} D={}",
            context.tipoCompromisso, context.compromisso.id, context.diasAteVencimento)

        return when (val result = engine.process(context)) {
            is UseCaseResult.Success -> ResponseEntity.ok(
                ProcessResponse("SUCCESS", result.useCaseName,
                    result.notifications.size, result.message)
            )
            is UseCaseResult.Skipped -> ResponseEntity.ok(
                ProcessResponse("SKIPPED", result.useCaseName, message = result.reason)
            )
            is UseCaseResult.Failure -> ResponseEntity.internalServerError().body(
                ProcessResponse("FAILURE", result.useCaseName, message = result.message)
            )
        }
    }

    @PostMapping("/process/batch")
    fun processBatch(
        @Valid @RequestBody request: BatchProcessRequest
    ): ResponseEntity<BatchProcessResponse> {
        val contexts = request.compromissos.map {
            ProcessingContext(compromisso = it.toCompromisso())
        }
        val result = engine.processBatch(contexts)
        return ResponseEntity.ok(
            BatchProcessResponse(result.total, result.success, result.skipped, result.failures)
        )
    }
}
