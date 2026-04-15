package br.com.exemplo.compromissos.controller

import br.com.exemplo.compromissos.domain.Compromisso
import br.com.exemplo.compromissos.domain.Notificacao
import br.com.exemplo.compromissos.service.ProcessarCompromissoService
import br.com.exemplo.compromissos.service.RecuperarNotificacoesService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

// ---------------------------------------------------------------------------
// Controller — Fluxo 1
// ---------------------------------------------------------------------------

@RestController
@RequestMapping("/api/v1/compromissos")
class CompromissoController(
    private val processarService: ProcessarCompromissoService
) {

    @PostMapping
    fun processarCompromisso(
        @Valid @RequestBody request: CompromissoRequest
    ): ResponseEntity<ProcessarCompromissoResponse> {
        val compromisso = request.toDomain()
        val notificacoes = processarService.processar(compromisso)
        return ResponseEntity.ok(ProcessarCompromissoResponse.from(compromisso, notificacoes))
    }
}

// ---------------------------------------------------------------------------
// Controller — Fluxo 2
// ---------------------------------------------------------------------------

@RestController
@RequestMapping("/api/v1/notificacoes")
class NotificacaoController(
    private val recuperarService: RecuperarNotificacoesService
) {

    @GetMapping("/elegiveis")
    fun recuperarElegiveis(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        dataPublicacao: LocalDate
    ): ResponseEntity<List<NotificacaoResponse>> {
        val elegiveis = recuperarService.recuperarElegiveis(dataPublicacao)
        return ResponseEntity.ok(elegiveis.map { NotificacaoResponse.from(it) })
    }
}

// ---------------------------------------------------------------------------
// DTOs de Request
// ---------------------------------------------------------------------------

data class CompromissoRequest(

    @field:NotNull(message = "id_compromisso é obrigatório")
    val idCompromisso: UUID,

    @field:NotNull(message = "data_vencimento é obrigatória")
    val dataVencimento: LocalDate,

    @field:NotBlank(message = "nome_beneficiario é obrigatório")
    val nomeBeneficiario: String,

    @field:NotNull
    @field:Positive(message = "valor deve ser positivo")
    val valor: BigDecimal,

    @field:NotBlank(message = "metadado é obrigatório")
    val metadado: String
) {
    fun toDomain() = Compromisso(
        idCompromisso    = idCompromisso,
        dataVencimento   = dataVencimento,
        nomeBeneficiario = nomeBeneficiario,
        valor            = valor,
        metadado         = metadado
    )
}

// ---------------------------------------------------------------------------
// DTOs de Response
// ---------------------------------------------------------------------------

data class ProcessarCompromissoResponse(
    val idCompromisso: UUID,
    val useCasesElegiveis: List<String>,
    val totalNotificacoesSalvas: Int
) {
    companion object {
        fun from(compromisso: Compromisso, notificacoes: List<Notificacao>) =
            ProcessarCompromissoResponse(
                idCompromisso          = compromisso.idCompromisso,
                useCasesElegiveis      = notificacoes.map { it.useCaseId },
                totalNotificacoesSalvas = notificacoes.size
            )
    }
}

data class NotificacaoResponse(
    val idNotificacao: UUID,
    val idCompromisso: UUID,
    val dataVencimento: LocalDate,
    val nomeBeneficiario: String,
    val valor: BigDecimal,
    val metadado: String,
    val dataPublicacao: LocalDate,
    val useCaseId: String
) {
    companion object {
        fun from(n: Notificacao) = NotificacaoResponse(
            idNotificacao    = n.idNotificacao,
            idCompromisso    = n.idCompromisso,
            dataVencimento   = n.dataVencimento,
            nomeBeneficiario = n.nomeBeneficiario,
            valor            = n.valor,
            metadado         = n.metadado,
            dataPublicacao   = n.dataPublicacao,
            useCaseId        = n.useCaseId
        )
    }
}
