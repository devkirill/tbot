package com.project.tbot.parser

import com.project.tbot.bot.TBot
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import com.project.tbot.storage.service.Storage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection


@Service
class RssUpdate {
    @Autowired
    lateinit var bot: TBot

    @Autowired
    lateinit var storage: Storage

    @Autowired
    lateinit var parser: XPathParser

    fun splitImagesForParts(urls: List<String>): List<List<String>> {
        val count = urls.size
        if (count == 1) {
            return listOf(urls)
        }

        val result = mutableListOf<List<String>>()

        val parts = (count + 9) / 10
        for (i in 0 until parts) {
            val from = i * count / parts
            val to = (i + 1) * count / parts
            result.add(urls.subList(from, to))
        }

        return result
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 10 * 60 * 1000)
    fun update() {
        val sended = storage.getAll<Sended>()
        val list = storage.getAll<Subscribe>()

        list.map { subscribe ->
            val feed = parser.parseRss(getContent(subscribe.rss))

            for (post in feed.posts.reversed()) {
                val guid = post.guid ?: post.link
                val chatId = subscribe.chatId
                if (sended.none { it.chatId == chatId && it.guid == guid }) {
                    try {
                        val imageList = splitImagesForParts(post.images)
                        println(subscribe.chatId)

                        val s = SendMessage()
                        s.setChatId(subscribe.chatId)
                        s.text = post.description
//                        s.enableHtml(true)
                        if (post.link.isNotBlank()) {
                            val linkButton = InlineKeyboardButton(post.title)
                            linkButton.url = post.link

                            val keyboard = InlineKeyboardMarkup(mutableListOf(mutableListOf(linkButton)))

                            s.replyMarkup = keyboard
                        }
                        bot.execute(s)

                        storage.save(Sended(chatId = chatId, guid = guid))
                    } catch (e: TelegramApiException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun getContent(uri: String): InputStream {
        return try {
            val url = URL(uri)
            val urlConnection: URLConnection = url.openConnection()
            BufferedInputStream(urlConnection.getInputStream())
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}