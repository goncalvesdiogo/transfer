package com.example.recommendation.message

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageTemplateLoader(
    private val objectMapper: ObjectMapper
) {
    private val cache = ConcurrentHashMap<String, String>()

    /**
     * Loads the message template for a given key.
     * Key format: "use_case.tier" — maps to resources/templates/recommendations/use_case/tier.json
     *
     * Example: "boleto_vence_amanha.single" → templates/recommendations/boleto_vence_amanha/single.json
     */
    fun load(key: String): String = cache.getOrPut(key) {
        val path = "templates/recommendations/${key.replace('.', '/')}.json"
        val resource = ClassPathResource(path)

        require(resource.exists()) {
            "Template não encontrado para a chave '$key' em '$path'"
        }

        val node = objectMapper.readTree(resource.inputStream)
        node["message"]?.asText()
            ?: error("Campo 'message' ausente no template '$path'")
    }
}
