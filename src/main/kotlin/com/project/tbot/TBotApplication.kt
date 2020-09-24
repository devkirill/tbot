package com.project.tbot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.LongPollingBot
import javax.annotation.PostConstruct

@SpringBootApplication
class TBotApplication {
    @Value("\${token}")
    lateinit var token: String

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