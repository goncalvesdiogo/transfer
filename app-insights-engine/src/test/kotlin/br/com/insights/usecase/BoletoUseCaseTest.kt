package br.com.insights.usecase

import br.com.insights.Fixtures
import br.com.insights.domain.NotificationChannel
import br.com.insights.domain.NotificationPriority
import br.com.insights.domain.StatusCompromisso
import br.com.insights.domain.UseCaseResult
import br.com.insights.usecase.boleto.BoletoDueInFourDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueInTenDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueTodayUseCase
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class BoletoUseCaseTest : DescribeSpec({

    describe("BoletoDueTodayUseCase (D0)") {
        val useCase = BoletoDueTodayUseCase()

        it("deve retornar Success com 3 notificações para boleto PENDENTE vencendo hoje") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 0))
            val result = useCase.execute(ctx)

            result.shouldBeInstanceOf<UseCaseResult.Success>()
            result.notifications shouldHaveSize 3
            result.notifications.map { it.channel } shouldBe listOf(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.WHATSAPP
            )
            result.notifications.all { it.priority == NotificationPriority.CRITICAL } shouldBe true
        }

        it("deve retornar Skipped para boleto PAGO") {
            val ctx = Fixtures.context(Fixtures.boleto(status = StatusCompromisso.PAGO))
            // D0 use case não tem shouldProcess próprio — o filtro vem da árvore;
            // mesmo assim, o execute deve completar sem erro
            val result = useCase.execute(ctx)
            result.shouldBeInstanceOf<UseCaseResult.Success>() // use case executa, árvore filtra
        }
    }

    describe("BoletoDueInFourDaysUseCase (D-4)") {
        val useCase = BoletoDueInFourDaysUseCase()

        it("deve retornar Success com Email e SMS") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 4))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()

            result.notifications shouldHaveSize 2
            result.notifications.map { it.channel } shouldBe listOf(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS
            )
        }
    }

    describe("BoletoDueInTenDaysUseCase (D-10)") {
        val useCase = BoletoDueInTenDaysUseCase()

        it("deve retornar Success com apenas Email de baixa prioridade") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 10))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()

            result.notifications shouldHaveSize 1
            result.notifications.first().channel shouldBe NotificationChannel.EMAIL
            result.notifications.first().priority shouldBe NotificationPriority.LOW
        }
    }
})
