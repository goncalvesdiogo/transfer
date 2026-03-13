package br.com.insights.usecase.boleto

import br.com.insights.domain.Boleto
import br.com.insights.domain.ProcessingContext
import br.com.insights.usecase.BaseNotificationUseCase

abstract class BaseBoletoUseCase : BaseNotificationUseCase() {
    protected fun ProcessingContext.boleto(): Boleto = compromissoAs()
}
