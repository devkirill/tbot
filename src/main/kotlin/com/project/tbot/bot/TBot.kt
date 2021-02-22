package com.project.tbot.bot

import com.project.tbot.parser.RssUpdate
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import com.project.tbot.storage.service.Storage
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
    lateinit var rssUpdate: RssUpdate

    override fun getBotToken() = token

    override fun getBotUsername() = "RSS bot"

    override fun onUpdateReceived(update: Update) {
        val msg = update.message

        val txt: String = msg.text
        when {
            txt.trim() == "/start" -> {
                msg.sendMsg("Hello, world! This is simple bot!")
            }
            txt.startsWith("/add ") -> {
                val url = txt.substringAfter(" ")
                val chatId = msg.chatId
                val subscribe = Subscribe(chatId, url)

                storage.save(subscribe)
                val feed = rssUpdate.getFeed(subscribe)

                for (post in feed.posts.subList(0, 3).reversed()) {
                    rssUpdate.send(chatId, post)
                }
                val othersPosts = feed.posts.subList(3, feed.posts.size)
                othersPosts.forEach { post ->
                    val guid = post.guid ?: post.link
                    storage.save(Sended(chatId = chatId, guid = guid))
                }
            }
        }
    }

    //    @Scheduled(initialDelay = 2000, fixedDelay = 10 * 60 * 1000)
//    fun update() {
//        val subscribes = storage.getAll<Subscribe>().groupBy({ it.url }) { it.chatId }
//        val mangas = storage.getAll<Manga>().map { it.url to it.latest }.toMap().toMutableMap()
//        for (s in subscribes) {
//            val latest = mangas[s.key] ?: "0"
//            val url = URL(s.key)
//            when (url.host) {
//                "naruto-base.su" -> {
//                    val latestId = latest.replace(Regex("\\D"), "").toInt()
//                    val list = Jsoup.connect(s.key).get()
//                            .select("#allEntries div.title a")
//                            .map { it.text() to "https://naruto-base.su${it.attr("href")}" }
//                            .filter { it.first.replace(Regex("\\D"), "").toInt() > latestId }
//                            .sortedBy { it.first.replace(Regex("\\D"), "").toInt() }
//                    if (list.isNotEmpty()) {
//                        var newLatest = latest
//                        for (i in list) {
//                            val doc = Jsoup.connect(i.second).get()
//                            val urls = doc.select("div.yellowBox div a[rel~=iLoad]")
//                                    .map { element ->
//                                        val href = element.attr("href")
//                                        "https://naruto-base.su$href"
//                                    }
//                            if (urls.isNotEmpty()) {
//                                for (chatId in s.value) {
//                                    sendImages(chatId, urls)
//                                }
//                                newLatest = i.first
//                            }
//                        }
//                        if (latest != newLatest)
//                            storage.save(Manga(s.key, newLatest))
//                    }
//                }
//            }
//        }
//    }
//
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

//    private fun sendImage(chatId: Long, url: String) {
//        try {
//            println(chatId)
//
//            val s = SendPhoto()
//            s.setChatId(chatId)
//            s.setPhoto(url, URL(url).openStream())
//            execute(s)
//        } catch (e: TelegramApiException) {
//            e.printStackTrace()
//        }
//    }
//
//    private infix fun Message.sendImage(url: String) = sendImage(chatId, url)
//
//    private fun sendImages(chatId: Long, urls: List<String>) {
//        val count = urls.size
//        if (count == 1) {
//            sendImage(chatId, urls.first())
//            return
//        }
//
//        fun sendPart(urls: List<String>) {
//            val s = SendMediaGroup()
//            s.setChatId(chatId)
//            println(chatId)
//            s.media = urls.map { url ->
//                InputMediaPhoto().setMedia(URL(url).openStream(), url)
//            }
//            try {
//                execute(s)
//            } catch (e: TelegramApiException) {
//                e.printStackTrace()
//            }
//        }
//
//        val parts = (count + 9) / 10
//        for (i in 0 until parts) {
//            val from = i * count / parts
//            val to = (i + 1) * count / parts
//            sendPart(urls.subList(from, to))
//        }
//    }
//
//    private infix fun Message.sendImages(urls: List<String>) = sendImages(chatId, urls)
}