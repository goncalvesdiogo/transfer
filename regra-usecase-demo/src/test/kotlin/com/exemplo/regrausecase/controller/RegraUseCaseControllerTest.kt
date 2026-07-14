package com.exemplo.regrausecase.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.http.MediaType

@SpringBootTest
@AutoConfigureMockMvc
class RegraUseCaseControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // Payload exatamente no formato recebido da base de casos de uso
    private val payloadJson = """
        {
          "id_caso_uso": "CTRL-001",
          "aplicavelA": ["BOLETO", "PARCELA"],
          "usecase_name": "BOLETO_D0",
          "ativo": true,
          "condicoes": [{"campo":"dt_vcto", "operacao": ">=", "valor": "${'$'}hoje"}],
          "modo_disparo": "INDIVIDUAL"
        }
    """.trimIndent()

    @Test
    fun `POST regras deve salvar payload e retornar 201`() {
        mockMvc.perform(
            post("/regras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.idCasoUso").value("CTRL-001"))
            .andExpect(jsonPath("$.usecaseName").value("BOLETO_D0"))
    }

    @Test
    fun `GET regras por id deve retornar a regra salva`() {
        mockMvc.perform(
            post("/regras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson.replace("CTRL-001", "CTRL-002"))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/regras/CTRL-002"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usecaseName").value("BOLETO_D0"))
            .andExpect(jsonPath("$.condicoes[0].campo").value("dt_vcto"))
    }

    @Test
    fun `GET regras por id inexistente deve retornar 404`() {
        mockMvc.perform(get("/regras/NAO-EXISTE-XYZ"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `POST regras validar deve retornar valido true para boleto vencendo hoje`() {
        mockMvc.perform(
            post("/regras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson.replace("CTRL-001", "CTRL-003"))
        ).andExpect(status().isCreated)

        val hoje = java.time.LocalDate.now()
        val boletoJson = """
            {"id":"BOL-X","dtVcto":"$hoje","valor":100.00,"tipo":"BOLETO"}
        """.trimIndent()

        mockMvc.perform(
            post("/regras/CTRL-003/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boletoJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valido").value(true))
    }
}
