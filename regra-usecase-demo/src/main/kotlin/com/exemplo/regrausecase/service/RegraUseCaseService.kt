package com.exemplo.regrausecase.service

import com.exemplo.regrausecase.dto.Condicao
import com.exemplo.regrausecase.dto.RegraUseCasePayload
import com.exemplo.regrausecase.model.ModoDisparo
import com.exemplo.regrausecase.model.RegraUseCaseEntity
import com.exemplo.regrausecase.repository.RegraUseCaseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegraUseCaseService(
    private val repository: RegraUseCaseRepository,
    private val validator: RegraUseCaseValidator
) {

    @Transactional
    fun salvar(payload: RegraUseCasePayload): RegraUseCaseEntity {
        val entidade = RegraUseCaseEntity(
            idCasoUso = payload.idCasoUso,
            aplicavelA = payload.aplicavelA,
            usecaseName = payload.usecaseName,
            ativo = payload.ativo,
            condicoes = payload.condicoes,
            modoDisparo = ModoDisparo.valueOf(payload.modoDisparo)
        )
        return repository.save(entidade)
    }

    @Transactional(readOnly = true)
    fun buscarPorId(idCasoUso: String): RegraUseCaseEntity? =
        repository.findById(idCasoUso).orElse(null)

    @Transactional(readOnly = true)
    fun buscarAtivasPorUsecase(usecaseName: String): List<RegraUseCaseEntity> =
        repository.findByUsecaseNameAndAtivoTrue(usecaseName)

    /**
     * Busca a regra no banco pelo id e valida a entidade informada contra
     * as condicoes armazenadas.
     */
    @Transactional(readOnly = true)
    fun validarEntidadeContraRegra(idCasoUso: String, entidade: Any): ResultadoValidacao {
        val regra = buscarPorId(idCasoUso)
            ?: return ResultadoValidacao(valido = false, motivo = "Regra '$idCasoUso' nao encontrada")

        return validator.validar(regra.condicoes, entidade, regra.ativo)
    }

    /**
     * Variante que recebe as condicoes diretamente (util quando a regra
     * ainda nao foi persistida, ou para validacao rapida/dry-run).
     */
    fun validarCondicoes(condicoes: List<Condicao>, entidade: Any, ativo: Boolean = true): ResultadoValidacao =
        validator.validar(condicoes, entidade, ativo)
}
