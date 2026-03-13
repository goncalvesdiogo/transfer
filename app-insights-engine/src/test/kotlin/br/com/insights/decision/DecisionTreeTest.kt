package br.com.insights.decision

import br.com.insights.Fixtures
import br.com.insights.domain.StatusCompromisso
import br.com.insights.usecase.boleto.BoletoDueInFourDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueInTenDaysUseCase
import br.com.insights.usecase.boleto.BoletoDueTodayUseCase
import br.com.insights.usecase.parcela.ParcelaDueInSevenDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueInThreeDaysUseCase
import br.com.insights.usecase.parcela.ParcelaDueTodayUseCase
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class DecisionTreeTest : DescribeSpec({

    // ── Instâncias dos use cases ─────────────────────────────────────────────
    val boletoFactory = BoletoDecisionTreeFactory(
        BoletoDueTodayUseCase(), BoletoDueInFourDaysUseCase(), BoletoDueInTenDaysUseCase()
    )
    val parcelaFactory = ParcelaDecisionTreeFactory(
        ParcelaDueTodayUseCase(), ParcelaDueInThreeDaysUseCase(), ParcelaDueInSevenDaysUseCase()
    )

    describe("BoletoDecisionTree") {
        val tree = boletoFactory.create()

        it("resolve D0 para boleto vencendo hoje") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 0))
            tree.resolve(ctx).shouldBeInstanceOf<BoletoDueTodayUseCase>()
        }

        it("resolve D-4 para boleto vencendo em 4 dias") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 4))
            tree.resolve(ctx).shouldBeInstanceOf<BoletoDueInFourDaysUseCase>()
        }

        it("resolve D-10 para boleto vencendo em 10 dias") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 10))
            tree.resolve(ctx).shouldBeInstanceOf<BoletoDueInTenDaysUseCase>()
        }

        it("retorna null para boleto PAGO") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 0, status = StatusCompromisso.PAGO))
            tree.resolve(ctx) shouldBe null
        }

        it("retorna null para D-7 (sem regra configurada para boleto)") {
            val ctx = Fixtures.context(Fixtures.boleto(daysOffset = 7))
            tree.resolve(ctx) shouldBe null
        }
    }

    describe("ParcelaDecisionTree") {
        val tree = parcelaFactory.create()

        it("resolve D0") {
            tree.resolve(Fixtures.context(Fixtures.parcela(daysOffset = 0)))
                .shouldBeInstanceOf<ParcelaDueTodayUseCase>()
        }

        it("resolve D-3") {
            tree.resolve(Fixtures.context(Fixtures.parcela(daysOffset = 3)))
                .shouldBeInstanceOf<ParcelaDueInThreeDaysUseCase>()
        }

        it("resolve D-7") {
            tree.resolve(Fixtures.context(Fixtures.parcela(daysOffset = 7)))
                .shouldBeInstanceOf<ParcelaDueInSevenDaysUseCase>()
        }

        it("parcela D-4 retorna null (boleto usa D-4, parcela não)") {
            tree.resolve(Fixtures.context(Fixtures.parcela(daysOffset = 4))) shouldBe null
        }

        it("parcela RENEGOCIADA é processada") {
            val ctx = Fixtures.context(
                Fixtures.parcela(daysOffset = 0, status = StatusCompromisso.RENEGOCIADO)
            )
            tree.resolve(ctx).shouldBeInstanceOf<ParcelaDueTodayUseCase>()
        }
    }
})
