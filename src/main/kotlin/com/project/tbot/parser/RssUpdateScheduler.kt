package com.project.tbot.parser

import com.project.rss.service.TemplateStorage
import com.project.tbot.bot.TBot
import com.project.tbot.parser.model.Feed
import com.project.tbot.parser.model.Post
import com.project.tbot.storage.Storage
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import com.project.tbot.utils.toFeed
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection


@Service
class RssUpdateScheduler {
    @Autowired
    lateinit var bot: TBot

    @Autowired
    lateinit var storage: Storage

    @Autowired
    lateinit var parser: XPathParser

    @Autowired
    lateinit var templateStorage: TemplateStorage

    val parsers by lazy {
        mapOf(
            "html" to { url: String -> templateStorage.findParser(url).getFeed(url).toFeed() },
            "" to { url: String -> parser.parseRss(getContent(url)) }
        )
    }

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
        val list = storage.getAllSubscribed()

        list.map { subscribe ->
            try {
                val feed = getFeed(subscribe)

                for (post in feed.posts.reversed()) {
                    val chatId = subscribe.chatId

                    send(chatId, feed, post)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun getFeed(subscribe: Subscribe): Feed {
        return parsers[subscribe.type]?.let { it(subscribe.rss) } ?: TODO()
    }

    fun send(chatId: Long, feed: Feed, post: Post) {
        val sended = Sended(chatId = chatId, guid = post.guid ?: post.link)
        if (storage.alreadySend(sended)) {
            return;
        }

        try {
            println(chatId)

            post
                .let { post ->
                    post.copy(images = post.images.filter { !it.endsWith(".svg") })
                }
                .let { post ->
                    when {
                        post.images.isEmpty() && feed.image.isNotBlank() && post.isHabr() -> {
                            val document = Jsoup.parse(URL(post.link), 5000)

                            val images = mutableListOf<Pair<Int, String>>()

                            val imageSrc = document.select("link[rel*=image_src]")
                            if (imageSrc.isNotEmpty()) {
                                val link = imageSrc[0].attr("href")
                                val order = if ("habr.com" !in link) 1 else 5
                                images += order to link
                            }
                            val res = document.select("div.tm-article-body img")
                            if (res.isNotEmpty()) {
                                images += 2 to if (res[0].hasAttr("data-src")) {
                                    res[0].attr("data-src")
                                } else {
                                    res[0].attr("src")
                                }
                            }
                        if (images.isNotEmpty()) {
                            post.copy(images = listOf(images.minByOrNull { it.first }!!.second))
                        } else {
                            post
                        }
                    }

                    post.images.isEmpty() && feed.image.isNotBlank() -> {
                        post.copy(images = listOf(feed.image))
                    }

                    else -> post
                }
            }.let { post ->
                when {
                    post.isHabr() -> {
                        post.copy(
                            description = post.description.replace(
                                Regex("\\s+\\[[^]]*\\]\\([^)]*\\)\\s*$"),
                                ""
                            )
                        )
                    }

                    else -> post
                }
            }.let { post ->
                when {
                    post.images.isNotEmpty() -> {
                        val groups = mutableListOf<Any>()
                        val url = post.images.first()
                        val photo = SendPhoto()
                        photo.setPhoto(url, URL(url).openStream())
                        groups.add(photo)

                        val imageList = splitImagesForParts(post.images.drop(1))
                        println(imageList)

                        for (list in imageList) {
                            val group = SendMediaGroup()
                            group.media = list.map { InputMediaPhoto().setMedia(URL(it).openStream(), it) }
                            groups.add(group)
                        }

                        groups
                    }

                    else -> {
                        listOf(SendMessage())
                    }
                }.let { groups ->
                    groups + post.files.map {
                        val file = SendDocument()
                        val pair = getContentFile(it)
                        file.document = InputFile(pair.second, pair.first)
                        file
                    }
                }.let { groups ->
                    groups.first().let { mainDesc ->
                        when (mainDesc) {
                            is SendMessage -> {
                                mainDesc.text = post.description
                                mainDesc.enableMarkdownV2(true)
                            }

                            is SendPhoto -> {
                                mainDesc.caption = post.description
                                mainDesc.parseMode = "MarkdownV2"
                            }

                            is SendMediaGroup -> {
                                mainDesc.media.first().caption = post.description
                            }
                        }

                        if (post.link.isNotBlank()) {
                            val title = if (post.title.isBlank()) post.link else post.title
                            val linkButton = InlineKeyboardButton(title)
                            linkButton.url = post.link

                            val keyboard = InlineKeyboardMarkup(mutableListOf(mutableListOf(linkButton)))

                            when (mainDesc) {
                                is SendMessage -> mainDesc.replyMarkup = keyboard
                                is SendPhoto -> mainDesc.replyMarkup = keyboard
                            }
                        }
                    }

                    groups
                }
            }.let { groups ->
                groups.forEach { msg ->
                    if (msg is SendPhoto && msg.caption.length > 1000) {
                        var caption = msg.caption.reversed()
                        while (caption.length > 1000) {
                            val brackets = mutableListOf<Char>()
                            var index = 0
                            while (index == 0 || caption[index] !in " \n" || brackets.isNotEmpty()) {
                                if (caption[index] in "{}[]()") {
                                    val c = caption[index]
                                    val pair = listOf("{}", "[]", "()")
                                        .flatMap { listOf(it[0] to it[1], it[1] to it[0]) }
                                        .toMap()
                                    if (brackets.isNotEmpty() && brackets.last() == pair[c]) {
                                        brackets.removeLast()
                                    } else {
                                        brackets += caption[index]
                                    }
                                }
                                index += 1
                            }
                            caption = caption.substring(index)
                        }
                        msg.caption = caption.reversed()
                    }
                }
                groups
            }.let { groups ->
                for (msg in groups) {
                    when (msg) {
                        is SendMessage -> msg.setChatId(chatId)
                        is SendPhoto -> msg.setChatId(chatId)
                        is SendMediaGroup -> msg.setChatId(chatId)
                        is SendDocument -> msg.setChatId(chatId)
                    }
                }
                groups
            }.let { groups ->
                    fun send(msg: Any, retry: Int = 2) {
                        try {
                            when (msg) {
                                is SendMessage -> bot.execute(msg)
                                // TODO - split msg if >1024
                                is SendPhoto -> bot.execute(msg)
                                is SendMediaGroup -> bot.execute(msg)
                                is SendDocument -> bot.execute(msg)
                            }
                        } catch (e: TelegramApiException) {
                            System.err.println(msg.toString())
                            e.printStackTrace()
                            if (retry > 0) {
                                send(msg, retry - 1)
                            } else {
                                throw e
                            }
                        }
                    }
                    for (msg in groups) {
                        send(msg)
                    }
                }

            storage.save(sended)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    fun getContent(uri: String): InputStream {
        return try {
            System.setProperty("http.agent", "insomnia/6.6.2")
            val url = URL(uri)
            val urlConnection: URLConnection = url.openConnection()
            urlConnection.setRequestProperty("accept", "*/*")
            urlConnection.setRequestProperty("user-agent", "insomnia/2020.5.2")
//            urlConnection.setRequestProperty("Host", "mangalib.me")
            BufferedInputStream(urlConnection.getInputStream())
        } catch (e: Exception) {
            throw IllegalStateException("error with get from $uri", e)
        }
    }

    fun getContentFile(uri: String): Pair<String, InputStream> {
        return try {
            System.setProperty("http.agent", "insomnia/6.6.2")

            val url = URL(uri)
            val urlConnection = url.openConnection()

            urlConnection.setRequestProperty("accept", "*/*")
            urlConnection.setRequestProperty("user-agent", "insomnia/2020.5.2")

            val fieldValue = urlConnection.getHeaderField("Content-Disposition")
            if (fieldValue == null || !fieldValue.contains("filename=\"")) {
                // no file name there -> throw exception ...
            }
            val filename = fieldValue!!.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length - 1)
//            val download = File(System.getProperty("java.io.tmpdir"), filename)

            Pair(filename, BufferedInputStream(urlConnection.getInputStream()))
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}

fun Post.isHabr() = link matches Regex("https?:\\/\\/habr\\.com\\/.*")