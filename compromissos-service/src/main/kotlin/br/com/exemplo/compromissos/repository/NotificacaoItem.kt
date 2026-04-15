package br.com.exemplo.compromissos.repository

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import java.math.BigDecimal
import java.time.LocalDate

// ---------------------------------------------------------------------------
// DynamoDB Item — mapeado pelo Enhanced Client
// Precisa de construtor sem argumentos (garantido pelo plugin all-open+noarg)
// ---------------------------------------------------------------------------

@DynamoDbBean
data class NotificacaoItem(

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("id_notificacao")
    var idNotificacao: String = "",

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("sk")
    var sk: String = "",               // idNotificacao#dataVencimento

    @get:DynamoDbAttribute("id_compromisso")
    var idCompromisso: String = "",

    @get:DynamoDbAttribute("data_vencimento")
    var dataVencimento: String = "",   // ISO-8601: yyyy-MM-dd

    @get:DynamoDbAttribute("nome_beneficiario")
    var nomeBeneficiario: String = "",

    @get:DynamoDbAttribute("valor")
    var valor: BigDecimal = BigDecimal.ZERO,

    @get:DynamoDbAttribute("metadado")
    var metadado: String = "",

    @get:DynamoDbAttribute("data_publicacao")
    var dataPublicacao: String = "",   // ISO-8601: yyyy-MM-dd

    @get:DynamoDbAttribute("use_case_id")
    var useCaseId: String = ""
)
