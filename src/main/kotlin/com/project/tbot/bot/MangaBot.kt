package com.project.tbot.bot

import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.net.URL


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
            msg.sendMsg("Hello, world! This is simple bot!")
        } else if (txt == "one-piece") {
            val doc = Jsoup.connect("https://naruto-base.su/news/manga_van_pis_990_glava/2020-09-11-6318").get()
            msg.sendMsg(doc.title())
            val urls = doc.select("div.yellowBox div a[rel~=iLoad]")
                    .map { element ->
                        val href = element.attr("href")
                        "https://naruto-base.su$href"
                    }
            msg.sendImages(urls)
        }
    }

    private fun Message.sendMsg(text: String) {
        val s = SendMessage()
        s.setChatId(chatId) // Боту может писать не один человек, и поэтому чтобы отправить сообщение, грубо говоря нужно узнать куда его отправлять
        println(chatId)
        s.text = text
        try { //Чтобы не крашнулась программа при вылете Exception
            execute(s)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun Message.sendImage(url: String) {
        val s = SendPhoto()
        s.setChatId(chatId)
        println(chatId)
        s.setPhoto(url, URL(url).openStream())
        try {
            execute(s)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun Message.sendImages(urls: List<String>) {
        val count = urls.size
        if (count == 1) {
            this.sendImage(urls.first())
            return
        }

        fun sendPart(urls: List<String>) {
            val s = SendMediaGroup()
            s.setChatId(chatId)
            println(chatId)
            s.media = urls.map { url ->
                InputMediaPhoto().setMedia(URL(url).openStream(), url)
            }
            try {
                execute(s)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }

        val parts = (count + 9) / 10
        for (i in 0 until parts) {
            val from = i * count / parts
            val to = (i + 1) * count / parts
            sendPart(urls.subList(from, to))
        }
    }
}