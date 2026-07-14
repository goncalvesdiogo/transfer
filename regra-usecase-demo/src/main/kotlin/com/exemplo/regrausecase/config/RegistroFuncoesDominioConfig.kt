package com.exemplo.regrausecase.config

import com.exemplo.regrausecase.dominio.FuncoesDominio
import com.exemplo.regrausecase.service.FuncaoResolver
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * Registra, na inicializacao da aplicacao, funcoes de dominio ja existentes
 * no codigo para uso dentro das condicoes das regras. Mantem o FuncaoResolver
 * agnostico de dominio - ele so sabe "resolver expressoes", quem decide QUAIS
 * funcoes de negocio ficam disponiveis e essa classe.
 */
@Component
class RegistroFuncoesDominioConfig(private val funcaoResolver: FuncaoResolver) {

    @PostConstruct
    fun registrarFuncoes() {
        funcaoResolver.registrarFuncaoExistente("proximoDiaUtil", FuncoesDominio::proximoDiaUtil)
        funcaoResolver.registrarFuncaoExistente("valorComDesconto", FuncoesDominio::valorComDesconto)
    }
}
