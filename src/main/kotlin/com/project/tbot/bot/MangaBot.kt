package com.project.tbot.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class MangaBot : TelegramLongPollingBot() {
    @Value("\${token}")
    lateinit var token: String

    override fun getBotToken() = token

    override fun getBotUsername() = "NAME"

    override fun onUpdateReceived(update: Update) {
        val msg = update.message

        val txt: String = msg.text
        if (txt == "/start") {
            sendMsg(msg, "Hello, world! This is simple bot!")
        }
    }

    private fun sendMsg(msg: Message, text: String) {
        val s = SendMessage()
        s.setChatId(msg.chatId) // Боту может писать не один человек, и поэтому чтобы отправить сообщение, грубо говоря нужно узнать куда его отправлять
        s.text = text
        try { //Чтобы не крашнулась программа при вылете Exception
            execute(s)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}