package br.com.insights.usecase.parcela

import br.com.insights.domain.ParcelaFinanciamento
import br.com.insights.domain.ProcessingContext
import br.com.insights.usecase.BaseNotificationUseCase

abstract class BaseParcelaUseCase : BaseNotificationUseCase() {
    protected fun ProcessingContext.parcela(): ParcelaFinanciamento = compromissoAs()
    protected fun ParcelaFinanciamento.progresso() =
        "Parcela $numeroParcela/$totalParcelas (${"%.1f".format(percentualQuitado)}% quitado)"
}
