package com.exemplo.regrausecase.controller

import com.exemplo.regrausecase.dto.BoletoRequest
import com.exemplo.regrausecase.dto.RegraUseCasePayload
import com.exemplo.regrausecase.dto.ResultadoValidacaoResponse
import com.exemplo.regrausecase.model.Boleto
import com.exemplo.regrausecase.model.RegraUseCaseEntity
import com.exemplo.regrausecase.service.RegraUseCaseService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/regras")
class RegraUseCaseController(
    private val service: RegraUseCaseService
) {

    @PostMapping
    fun salvar(@Valid @RequestBody payload: RegraUseCasePayload): ResponseEntity<RegraUseCaseEntity> {
        val salva = service.salvar(payload)
        return ResponseEntity.status(HttpStatus.CREATED).body(salva)
    }

    @GetMapping("/{idCasoUso}")
    fun buscarPorId(@PathVariable idCasoUso: String): ResponseEntity<RegraUseCaseEntity> {
        val regra = service.buscarPorId(idCasoUso)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(regra)
    }

    @GetMapping("/usecase/{usecaseName}")
    fun buscarAtivasPorUsecase(@PathVariable usecaseName: String): ResponseEntity<List<RegraUseCaseEntity>> =
        ResponseEntity.ok(service.buscarAtivasPorUsecase(usecaseName))

    /**
     * Busca a regra pelo id no banco e valida o boleto informado
     * contra as condicoes armazenadas (ex: dt_vcto >= $hoje).
     */
    @PostMapping("/{idCasoUso}/validar")
    fun validar(
        @PathVariable idCasoUso: String,
        @Valid @RequestBody boletoRequest: BoletoRequest
    ): ResponseEntity<ResultadoValidacaoResponse> {
        val boleto = Boleto(
            id = boletoRequest.id,
            dtVcto = boletoRequest.dtVcto,
            valor = boletoRequest.valor,
            tipo = boletoRequest.tipo
        )

        val regra = service.buscarPorId(idCasoUso)
        val resultado = service.validarEntidadeContraRegra(idCasoUso, boleto)

        return ResponseEntity.ok(
            ResultadoValidacaoResponse(
                idCasoUso = idCasoUso,
                usecaseName = regra?.usecaseName ?: "DESCONHECIDO",
                valido = resultado.valido,
                motivo = resultado.motivo
            )
        )
    }
}
