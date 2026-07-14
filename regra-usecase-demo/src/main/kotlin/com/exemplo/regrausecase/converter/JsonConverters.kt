package com.exemplo.regrausecase.converter

import com.exemplo.regrausecase.dto.Condicao
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val objectMapper: ObjectMapper = jacksonObjectMapper()

/**
 * Persiste List<Condicao> como uma coluna TEXT contendo JSON.
 * Evita criar uma tabela filha para um caso simples como este;
 * se no futuro precisar consultar por campo/operacao especifico,
 * vale migrar para uma tabela `condicao` normalizada.
 */
@Converter
class CondicaoListConverter : AttributeConverter<List<Condicao>, String> {

    override fun convertToDatabaseColumn(attribute: List<Condicao>?): String =
        objectMapper.writeValueAsString(attribute ?: emptyList<Condicao>())

    override fun convertToEntityAttribute(dbData: String?): List<Condicao> {
        if (dbData.isNullOrBlank()) return emptyList()
        return objectMapper.readValue(dbData)
    }
}

/**
 * Persiste List<String> (ex: aplicavelA) como coluna TEXT contendo JSON.
 */
@Converter
class StringListConverter : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>?): String =
        objectMapper.writeValueAsString(attribute ?: emptyList<String>())

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) return emptyList()
        return objectMapper.readValue(dbData)
    }
}
