package com.exemplo.regrausecase.model

import com.exemplo.regrausecase.converter.CondicaoListConverter
import com.exemplo.regrausecase.converter.StringListConverter
import com.exemplo.regrausecase.dto.Condicao
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

enum class ModoDisparo {
    INDIVIDUAL,
    LOTE
}

@Entity
@Table(name = "regra_usecase")
class RegraUseCaseEntity(

    @Id
    @Column(name = "id_caso_uso", nullable = false, updatable = false)
    val idCasoUso: String,

    @Convert(converter = StringListConverter::class)
    @Column(name = "aplicavel_a", nullable = false, columnDefinition = "TEXT")
    val aplicavelA: List<String>,

    @Column(name = "usecase_name", nullable = false)
    val usecaseName: String,

    @Column(name = "ativo", nullable = false)
    val ativo: Boolean,

    @Convert(converter = CondicaoListConverter::class)
    @Column(name = "condicoes", nullable = false, columnDefinition = "TEXT")
    val condicoes: List<Condicao>,

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_disparo", nullable = false)
    val modoDisparo: ModoDisparo,

    @Column(name = "criado_em", nullable = false)
    val criadoEm: Instant = Instant.now()
)
