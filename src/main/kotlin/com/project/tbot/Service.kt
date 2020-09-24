package com.project.tbot

import com.project.tbot.bot.MangaBot
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.LongPollingBot

@Service
class Service(@Value("\${token}") val token: String) {
    private final val botApi = TelegramBotsApi()

    private final fun register(bot: LongPollingBot) {
        try {
            botApi.registerBot(bot)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    init {
        ApiContextInitializer.init()

        register(MangaBot(token))
    }
}