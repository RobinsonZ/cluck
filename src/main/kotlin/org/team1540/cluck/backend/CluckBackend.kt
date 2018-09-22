@file:JvmName("CluckMain")

package org.team1540.cluck.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableWebSecurity
@EnableConfigurationProperties
class CluckBackend {
    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.setQueueCapacity(500)
        executor.setThreadNamePrefix("cluck-")
        executor.initialize()
        return executor
    }

    @Bean
    fun scheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.setThreadNamePrefix("cluck-scheduler-")
        scheduler.poolSize = 2
        return scheduler
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(CluckBackend::class.java, *args)
}
