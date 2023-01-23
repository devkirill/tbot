package com.project

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.LongPollingBot
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableScheduling
class TBotApplication {
    private final val botApi = TelegramBotsApi()

    @Autowired
    lateinit var bots: List<LongPollingBot>

    init {
        ApiContextInitializer.init()
    }

    @PostConstruct
    fun registerBot() {
        try {
            bots.forEach { botApi.registerBot(it) }
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}