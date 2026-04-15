package br.com.exemplo.compromissos.usecase

import br.com.exemplo.compromissos.domain.Compromisso
import br.com.exemplo.compromissos.domain.UseCaseResult
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

// ---------------------------------------------------------------------------
// Contrato central — Strategy
// Adicionar um novo caso de uso = implementar esta interface e anotá-la com
// @Component (ou registrá-la manualmente no UseCaseRegistry).
// ---------------------------------------------------------------------------

interface CompromissoUseCase {
    /** Identificador único e legível do caso de uso. */
    val id: String

    /** Retorna true quando o compromisso é elegível para este caso de uso. */
    fun isEligible(compromisso: Compromisso): UseCaseResult
}

// ---------------------------------------------------------------------------
// Registro de casos de uso — facilita a adição de novos sem alterar serviços
// ---------------------------------------------------------------------------

@Component
class UseCaseRegistry(useCases: List<CompromissoUseCase>) {

    // Spring injeta automaticamente todos os beans que implementam a interface.
    // A lista já chega ordenada conforme @Order ou a ordem de declaração.
    private val registry: List<CompromissoUseCase> = useCases

    fun evaluate(compromisso: Compromisso): List<UseCaseResult> =
        registry.map { it.isEligible(compromisso) }

    fun eligibleOnly(compromisso: Compromisso): List<UseCaseResult> =
        evaluate(compromisso).filter { it.eligible }
}

// ===========================================================================
// Implementações dos casos de uso
// Para adicionar um novo: crie uma classe, implemente CompromissoUseCase,
// anote com @Component — o Spring a injetará automaticamente no Registry.
// ===========================================================================

/**
 * UC-001 — Vencimento futuro
 * O compromisso é elegível quando ainda não venceu.
 */
@Component
class VencimentoFuturoUseCase : CompromissoUseCase {

    override val id = "UC-001-VENCIMENTO-FUTURO"

    override fun isEligible(compromisso: Compromisso): UseCaseResult {
        val hoje = LocalDate.now()
        val eligible = compromisso.dataVencimento.isAfter(hoje)
        return UseCaseResult(
            useCaseId = id,
            eligible = eligible,
            reason = if (!eligible) "Data de vencimento ${compromisso.dataVencimento} já passou (hoje: $hoje)" else null
        )
    }
}

/**
 * UC-002 — Valor mínimo
 * O compromisso é elegível quando o valor é maior que R$ 0,00.
 */
@Component
class ValorMinimoUseCase : CompromissoUseCase {

    override val id = "UC-002-VALOR-MINIMO"

    override fun isEligible(compromisso: Compromisso): UseCaseResult {
        val eligible = compromisso.valor > BigDecimal.ZERO
        return UseCaseResult(
            useCaseId = id,
            eligible = eligible,
            reason = if (!eligible) "Valor ${compromisso.valor} não é positivo" else null
        )
    }
}

/**
 * UC-003 — Beneficiário preenchido
 * O compromisso é elegível quando o nome do beneficiário não está em branco.
 */
@Component
class BeneficiarioPreenchidoUseCase : CompromissoUseCase {

    override val id = "UC-003-BENEFICIARIO-PREENCHIDO"

    override fun isEligible(compromisso: Compromisso): UseCaseResult {
        val eligible = compromisso.nomeBeneficiario.isNotBlank()
        return UseCaseResult(
            useCaseId = id,
            eligible = eligible,
            reason = if (!eligible) "Nome do beneficiário está em branco" else null
        )
    }
}

/**
 * UC-004 — Metadado presente
 * O compromisso é elegível quando o campo metadado não está vazio.
 *
 * Exemplo de como adicionar um novo caso de uso sem alterar nada além deste arquivo.
 */
@Component
class MetadadoPresenteUseCase : CompromissoUseCase {

    override val id = "UC-004-METADADO-PRESENTE"

    override fun isEligible(compromisso: Compromisso): UseCaseResult {
        val eligible = compromisso.metadado.isNotBlank()
        return UseCaseResult(
            useCaseId = id,
            eligible = eligible,
            reason = if (!eligible) "Metadado está vazio" else null
        )
    }
}
