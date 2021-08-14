package com.project.tbot.bot

import com.project.tbot.parser.RssUpdateScheduler
import com.project.tbot.storage.Storage
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class TBot : TelegramLongPollingBot() {
    @Value("\${token:}")
    lateinit var token: String

    @Autowired
    lateinit var storage: Storage

    @Autowired
    lateinit var rssUpdate: RssUpdateScheduler

    override fun getBotToken() = token

    override fun getBotUsername() = "RSS bot"

    override fun onUpdateReceived(update: Update) {
        val msg = update.message

        val txt: String = msg.text
        when {
            txt.trim() == "/start" -> {
                msg.sendMsg("Hello, world! This is simple bot!")
            }
            txt.startsWith("/add") -> {
                val command = txt.substringBefore(" ")
                val type = if ("_" in command) command.substringAfter("_") else ""
                if (type != "" && type !in rssUpdate.parsers) {
                    msg.sendMsg("Неизвестный тип $type")
                    return
                }
                val url = txt.substringAfter(" ")
                val chatId = msg.chatId
                val subscribe = Subscribe(chatId = chatId, type = type, rss = url)

                storage.save(subscribe)
                val feed = rssUpdate.getFeed(subscribe)

                try {
                    for (post in feed.posts.subList(0, 1).reversed()) {
                        rssUpdate.send(chatId, post)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                val othersPosts = feed.posts.subList(1, feed.posts.size)
                othersPosts.forEach { post ->
                    val guid = post.guid ?: post.link
                    storage.save(Sended(chatId = chatId, guid = guid))
                }
            }
        }
    }

    fun sendMsg(chatId: Long, text: String) {
        try {
            println(chatId)

            val s = SendMessage()
            s.setChatId(chatId)
            s.text = text
            execute(s)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private infix fun Message.sendMsg(text: String) = sendMsg(chatId, text)
}