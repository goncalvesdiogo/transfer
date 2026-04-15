package br.com.exemplo.compromissos.repository

import br.com.exemplo.compromissos.domain.Notificacao
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.LocalDate
import java.util.UUID

// ---------------------------------------------------------------------------
// Porta — permite trocar a implementação sem afetar os serviços
// ---------------------------------------------------------------------------

interface NotificacaoRepository {
    fun salvar(notificacao: Notificacao)
    fun salvarTodas(notificacoes: List<Notificacao>)
    fun buscarPorDataPublicacao(dataPublicacao: LocalDate): List<Notificacao>
}

// ---------------------------------------------------------------------------
// Adaptador DynamoDB
// ---------------------------------------------------------------------------

@Repository
class DynamoDbNotificacaoRepository(
    private val enhancedClient: DynamoDbEnhancedClient
) : NotificacaoRepository {

    companion object {
        const val TABLE_NAME = "notificacoes"
    }

    private val table: DynamoDbTable<NotificacaoItem> by lazy {
        enhancedClient.table(TABLE_NAME, TableSchema.fromBean(NotificacaoItem::class.java))
    }

    override fun salvar(notificacao: Notificacao) {
        table.putItem(notificacao.toItem())
    }

    override fun salvarTodas(notificacoes: List<Notificacao>) {
        // Batch write — DynamoDB aceita até 25 itens por batch
        notificacoes.chunked(25).forEach { chunk ->
            val writeBatch = enhancedClient.batchWriteItem { batchBuilder ->
                batchBuilder.addWriteBatch { wb ->
                    chunk.forEach { wb.addPutItem(table, it.toItem()) }
                }
            }
            // Log de itens não processados (retry não implementado neste exemplo)
            if (writeBatch.unprocessedPutItemsForTable(table).isNotEmpty()) {
                throw RuntimeException("Existem itens não processados no batch write do DynamoDB")
            }
        }
    }

    override fun buscarPorDataPublicacao(dataPublicacao: LocalDate): List<Notificacao> {
        val filterExpression = Expression.builder()
            .expression("data_publicacao = :dataPublicacao")
            .expressionValues(
                mapOf(":dataPublicacao" to AttributeValue.fromS(dataPublicacao.toString()))
            )
            .build()

        val request = ScanEnhancedRequest.builder()
            .filterExpression(filterExpression)
            .build()

        return table.scan(request)
            .items()
            .map { it.toDomain() }
            .toList()
    }
}

// ---------------------------------------------------------------------------
// Mappers — mantidos próximos ao repositório (camada de infraestrutura)
// ---------------------------------------------------------------------------

fun Notificacao.toItem() = NotificacaoItem(
    idNotificacao  = idNotificacao.toString(),
    sk             = sk,
    idCompromisso  = idCompromisso.toString(),
    dataVencimento = dataVencimento.toString(),
    nomeBeneficiario = nomeBeneficiario,
    valor          = valor,
    metadado       = metadado,
    dataPublicacao = dataPublicacao.toString(),
    useCaseId      = useCaseId
)

fun NotificacaoItem.toDomain() = Notificacao(
    idNotificacao    = UUID.fromString(idNotificacao),
    idCompromisso    = UUID.fromString(idCompromisso),
    dataVencimento   = LocalDate.parse(dataVencimento),
    nomeBeneficiario = nomeBeneficiario,
    valor            = valor,
    metadado         = metadado,
    dataPublicacao   = LocalDate.parse(dataPublicacao),
    useCaseId        = useCaseId
)
