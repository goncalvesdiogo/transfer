package br.com.insights.infra.persistence

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Registro de auditoria de cada processamento executado pelo motor.
 */
@Entity
@Table(name = "processing_records")
@EntityListeners(AuditingEntityListener::class)
class ProcessingRecord(

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = "",

    @Column(nullable = false)
    val compromissoId: String,

    @Column(nullable = false)
    val tipoCompromisso: String,

    @Column(nullable = false)
    val useCaseName: String,

    @Column(nullable = false)
    val resultStatus: String,

    @Column(length = 1000)
    val resultMessage: String? = null,

    val notificationsDispatched: Int = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val processedAt: Instant = Instant.now()
)

@Repository
interface ProcessingRecordRepository : JpaRepository<ProcessingRecord, String> {
    fun findByCompromissoId(compromissoId: String): List<ProcessingRecord>
    fun findByTipoCompromisso(tipo: String): List<ProcessingRecord>
}
