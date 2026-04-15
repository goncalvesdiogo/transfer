package br.com.exemplo.compromissos.service

import br.com.exemplo.compromissos.domain.Compromisso
import br.com.exemplo.compromissos.domain.Notificacao
import br.com.exemplo.compromissos.factory.NotificacaoFactory
import br.com.exemplo.compromissos.repository.NotificacaoRepository
import br.com.exemplo.compromissos.usecase.UseCaseRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

// ---------------------------------------------------------------------------
// Fluxo 1 — Receber compromisso, avaliar casos de uso, salvar notificações
// ---------------------------------------------------------------------------

@Service
class ProcessarCompromissoService(
    private val registry: UseCaseRegistry,
    private val factory: NotificacaoFactory,
    private val repository: NotificacaoRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Recebe um [Compromisso], avalia todos os casos de uso registrados e
     * persiste uma [Notificacao] para cada caso de uso em que o compromisso
     * for considerado elegível.
     *
     * @return lista das notificações persistidas
     */
    fun processar(compromisso: Compromisso): List<Notificacao> {
        log.info("Processando compromisso id=${compromisso.idCompromisso}")

        val resultados = registry.eligibleOnly(compromisso)

        if (resultados.isEmpty()) {
            log.info("Compromisso id=${compromisso.idCompromisso} não é elegível para nenhum caso de uso")
            return emptyList()
        }

        log.info(
            "Compromisso id=${compromisso.idCompromisso} elegível para: ${resultados.map { it.useCaseId }}"
        )

        val notificacoes = factory.criarParaUseCasesElegiveis(compromisso, resultados)
        repository.salvarTodas(notificacoes)

        log.info("${notificacoes.size} notificação(ões) salvas para id=${compromisso.idCompromisso}")
        return notificacoes
    }
}

// ---------------------------------------------------------------------------
// Fluxo 2 — Recuperar notificações por dataPublicacao e revalidar elegibilidade
// ---------------------------------------------------------------------------

@Service
class RecuperarNotificacoesService(
    private val registry: UseCaseRegistry,
    private val repository: NotificacaoRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Busca as notificações salvas com a [dataPublicacao] informada,
     * reconstrói o [Compromisso] original a partir de cada notificação e
     * revalida a elegibilidade no caso de uso que a originou.
     *
     * @return apenas as notificações que ainda são elegíveis
     */
    fun recuperarElegiveis(dataPublicacao: LocalDate): List<Notificacao> {
        log.info("Recuperando notificações para dataPublicacao=$dataPublicacao")

        val notificacoes = repository.buscarPorDataPublicacao(dataPublicacao)

        log.info("${notificacoes.size} notificação(ões) encontradas, iniciando revalidação")

        val elegiveis = notificacoes.filter { notificacao ->
            val compromisso = notificacao.toCompromisso()
            val resultado = registry.evaluate(compromisso)
                .firstOrNull { it.useCaseId == notificacao.useCaseId }

            val ainda = resultado?.eligible == true

            if (!ainda) {
                log.info(
                    "Notificacao id=${notificacao.idNotificacao} não é mais elegível " +
                    "para useCase=${notificacao.useCaseId}: ${resultado?.reason}"
                )
            }
            ainda
        }

        log.info("${elegiveis.size} notificação(ões) ainda elegíveis após revalidação")
        return elegiveis
    }
}

// ---------------------------------------------------------------------------
// Extensão auxiliar — reconstrói Compromisso a partir de uma Notificacao
// ---------------------------------------------------------------------------

private fun Notificacao.toCompromisso() = Compromisso(
    idCompromisso    = idCompromisso,
    dataVencimento   = dataVencimento,
    nomeBeneficiario = nomeBeneficiario,
    valor            = valor,
    metadado         = metadado
)
