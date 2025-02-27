package com.pbergman.ags

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class LlmAgentSystemApplication {
    @Bean
    fun applicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<LlmAgentSystemApplication>(*args)
        }
    }
}