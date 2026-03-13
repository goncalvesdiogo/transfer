package br.com.insights.usecase

import br.com.insights.Fixtures
import br.com.insights.domain.NotificationChannel
import br.com.insights.domain.NotificationPriority
import br.com.insights.domain.StatusCompromisso
import br.com.insights.domain.UseCaseResult
import br.com.insights.usecase.parcela.ParcelaDueInSevenDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueInThreeDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueTodayUseCase
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParcelaUseCaseTest : DescribeSpec({

    describe("ParcelaDueTodayUseCase (D0)") {
        val useCase = ParcelaDueTodayUseCase()

        it("deve retornar Success com 3 notificações CRITICAL") {
            val ctx = Fixtures.context(Fixtures.parcela(daysOffset = 0))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()

            result.notifications shouldHaveSize 3
            result.notifications.all { it.priority == NotificationPriority.CRITICAL } shouldBe true
        }

        it("deve incluir contratoId nos metadados") {
            val ctx = Fixtures.context(Fixtures.parcela(daysOffset = 0))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()
            val emailNotif = result.notifications.first { it.channel == NotificationChannel.EMAIL }
            emailNotif.metadata["contratoId"] shouldBe "FIN-042"
        }
    }

    describe("ParcelaDueInThreeDaysUseCase (D-3)") {
        val useCase = ParcelaDueInThreeDaysUseCase()

        it("deve retornar Email + SMS com prioridade HIGH") {
            val ctx = Fixtures.context(Fixtures.parcela(daysOffset = 3))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()

            result.notifications shouldHaveSize 2
            result.notifications.all { it.priority == NotificationPriority.HIGH } shouldBe true
        }
    }

    describe("ParcelaDueInSevenDaysUseCase (D-7)") {
        val useCase = ParcelaDueInSevenDaysUseCase()

        it("deve retornar apenas Email com prioridade MEDIUM") {
            val ctx = Fixtures.context(Fixtures.parcela(daysOffset = 7))
            val result = useCase.execute(ctx).shouldBeInstanceOf<UseCaseResult.Success>()

            result.notifications shouldHaveSize 1
            result.notifications.first().channel shouldBe NotificationChannel.EMAIL
            result.notifications.first().priority shouldBe NotificationPriority.MEDIUM
        }

        it("deve processar parcela RENEGOCIADA (status especial de financiamento)") {
            val ctx = Fixtures.context(
                Fixtures.parcela(daysOffset = 7, status = StatusCompromisso.RENEGOCIADO)
            )
            val result = useCase.execute(ctx)
            result.shouldBeInstanceOf<UseCaseResult.Success>()
        }
    }
})
