package br.com.exemplo.compromissos.factory

import br.com.exemplo.compromissos.domain.Compromisso
import br.com.exemplo.compromissos.domain.Notificacao
import br.com.exemplo.compromissos.domain.UseCaseResult
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

// ---------------------------------------------------------------------------
// Factory — isola a criação de Notificacao do restante da aplicação
// ---------------------------------------------------------------------------

@Component
class NotificacaoFactory {

    /**
     * Cria uma [Notificacao] para cada caso de uso elegível.
     * A [dataPublicacao] padrão é hoje, mas pode ser sobrescrita.
     */
    fun criarParaUseCasesElegiveis(
        compromisso: Compromisso,
        resultados: List<UseCaseResult>,
        dataPublicacao: LocalDate = LocalDate.now()
    ): List<Notificacao> =
        resultados
            .filter { it.eligible }
            .map { resultado ->
                Notificacao(
                    idNotificacao    = UUID.randomUUID(),
                    idCompromisso    = compromisso.idCompromisso,
                    dataVencimento   = compromisso.dataVencimento,
                    nomeBeneficiario = compromisso.nomeBeneficiario,
                    valor            = compromisso.valor,
                    metadado         = compromisso.metadado,
                    dataPublicacao   = dataPublicacao,
                    useCaseId        = resultado.useCaseId
                )
            }
}
