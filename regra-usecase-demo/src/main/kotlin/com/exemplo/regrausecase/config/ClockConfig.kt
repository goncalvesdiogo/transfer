package com.exemplo.regrausecase.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfig {

    /**
     * Clock do sistema por padrao. Em testes, sobrescrevemos este bean
     * com um Clock.fixed(...) para congelar a data e validar $hoje
     * de forma deterministica.
     */
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
