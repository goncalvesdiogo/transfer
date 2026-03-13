package br.com.insights.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Configuração central da aplicação.
 */
@Configuration
class AppConfig {

    @Bean("insightsTaskExecutor")
    fun taskExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize    = 4
        maxPoolSize     = 20
        queueCapacity   = 200
        //threadNamePrefix = "insights-async-"
        initialize()
    }

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}

/**
 * Anotação utilitária para abrir classes Kotlin em testes sem @MockK.
 * Registrada no kotlin-maven-allopen via pom.xml.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenForTesting
