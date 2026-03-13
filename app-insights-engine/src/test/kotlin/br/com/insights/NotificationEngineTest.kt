package br.com.insights

import br.com.insights.domain.UseCaseResult
import br.com.insights.gateway.NotificationGateway
import br.com.insights.routing.DecisionTreeRouter
import br.com.insights.service.NotificationEngine
import br.com.insights.usecase.boleto.BoletoDueTodayUseCase
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotificationEngineTest : DescribeSpec({

    val router  = mockk<DecisionTreeRouter>()
    val gateway = mockk<NotificationGateway>()
    val engine  = NotificationEngine(router, gateway)

    describe("NotificationEngine") {

        it("processa com sucesso quando use case e gateway funcionam") {
            val ctx     = Fixtures.context(Fixtures.boleto(daysOffset = 0))
            val useCase = BoletoDueTodayUseCase()

            every { router.resolve(ctx) } returns useCase
            every { gateway.sendAll(any()) } returns 3

            val result = engine.process(ctx)

            result.shouldBeInstanceOf<UseCaseResult.Success>()
            verify(exactly = 1) { gateway.sendAll(any()) }
        }

        it("retorna Skipped quando router não encontra use case") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 5))
            every { router.resolve(ctx) } returns null

            val result = engine.process(ctx)

            result.shouldBeInstanceOf<UseCaseResult.Skipped>()
            verify(exactly = 0) { gateway.sendAll(any()) }
        }

        it("processBatch agrega resultados corretamente") {
            val ctx1 = Fixtures.context(Fixtures.boleto(id = "B1", daysOffset = 0))
            val ctx2 = Fixtures.context(Fixtures.boleto(id = "B2", daysOffset = 5)) // sem regra

            every { router.resolve(ctx1) } returns BoletoDueTodayUseCase()
            every { router.resolve(ctx2) } returns null
            every { gateway.sendAll(any()) } returns 3

            val batch = engine.processBatch(listOf(ctx1, ctx2))

            batch.total   shouldBe 2
            batch.success shouldBe 1
            batch.skipped shouldBe 1
            batch.failures shouldBe 0
        }
    }
})
