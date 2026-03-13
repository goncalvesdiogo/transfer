package br.com.insights.usecase

import br.com.insights.domain.ProcessingContext
import br.com.insights.domain.UseCaseResult

/**
 * Contrato de todos os casos de uso de notificação.
 * Pattern: Strategy
 */
interface NotificationUseCase {
    val name: String
    fun execute(context: ProcessingContext): UseCaseResult
}
