package br.com.insights.domain

sealed class UseCaseResult {

    data class Success(
        val useCaseName: String,
        val notifications: List<Notification>,
        val message: String = "Executado com sucesso"
    ) : UseCaseResult()

    data class Skipped(
        val useCaseName: String,
        val reason: String
    ) : UseCaseResult()

    data class Failure(
        val useCaseName: String,
        val error: Throwable,
        val message: String = "Falha na execução"
    ) : UseCaseResult()
}
