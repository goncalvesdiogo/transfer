package com.exemplo.regrausecase.repository

import com.exemplo.regrausecase.model.RegraUseCaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegraUseCaseRepository : JpaRepository<RegraUseCaseEntity, String> {

    fun findByUsecaseNameAndAtivoTrue(usecaseName: String): List<RegraUseCaseEntity>

    fun findByAtivoTrue(): List<RegraUseCaseEntity>
}
