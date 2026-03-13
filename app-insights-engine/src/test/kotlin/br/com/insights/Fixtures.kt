package br.com.insights

import br.com.insights.domain.*
import java.math.BigDecimal
import java.time.LocalDate

object Fixtures {

    val cliente = Cliente("CLI-001", "João Silva", "joao@email.com", "+5511999990001")

    fun boleto(
        id: String = "BOL-001",
        daysOffset: Long = 0,
        status: StatusCompromisso = StatusCompromisso.PENDENTE,
        referenceDate: LocalDate = LocalDate.now()
    ) = Boleto(
        id             = id,
        dataVencimento = referenceDate.plusDays(daysOffset),
        valor          = BigDecimal("500.00"),
        status         = status,
        cliente        = cliente,
        linhaDigitavel = "1234.5678 9012.3456",
        banco          = "Bradesco",
        nossoNumero    = "000001"
    )

    fun parcela(
        id: String = "PAR-001",
        daysOffset: Long = 0,
        status: StatusCompromisso = StatusCompromisso.PENDENTE,
        referenceDate: LocalDate = LocalDate.now(),
        numeroParcela: Int = 12,
        totalParcelas: Int = 60
    ) = ParcelaFinanciamento(
        id             = id,
        dataVencimento = referenceDate.plusDays(daysOffset),
        valor          = BigDecimal("1200.00"),
        status         = status,
        cliente        = cliente,
        numeroParcela  = numeroParcela,
        totalParcelas  = totalParcelas,
        saldoDevedor   = BigDecimal("45000.00"),
        contratoId     = "FIN-042",
        tipoBem        = TipoBemFinanciado.VEICULO,
        taxaJurosMensal = BigDecimal("1.49")
    )

    fun context(compromisso: Compromisso, referenceDate: LocalDate = LocalDate.now()) =
        ProcessingContext(compromisso = compromisso, dataReferencia = referenceDate)
}
