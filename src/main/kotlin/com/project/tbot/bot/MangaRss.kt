//package com.project.tbot.bot
//
//import com.project.tbot.storage.service.Storage
//import org.jsoup.Jsoup
//import java.net.URL
//
//class MangaRss(val storage: Storage) {
//    fun update(notify: BotNotify, sub: Subscription) {
//        val latest = sub.lastVersion
//        val url = URL(sub.url)
//        when (url.host) {
//            "naruto-base.su" -> {
//                val latestId = latest.replace(Regex("\\D"), "").toInt()
//                val list = Jsoup.connect(sub.url).get()
//                    .select("#allEntries div.title a")
//                    .map { it.text() to "https://naruto-base.su${it.attr("href")}" }
//                    .filter { it.first.replace(Regex("\\D"), "").toInt() > latestId }
//                    .sortedBy { it.first.replace(Regex("\\D"), "").toInt() }
//                if (list.isNotEmpty()) {
//                    var newLatest = latest
//                    var images = mutableListOf<String>()
//                    for (i in list) {
//                        val doc = Jsoup.connect(i.second).get()
//                        val urls = doc.select("div.yellowBox div a[rel~=iLoad]")
//                            .map { element ->
//                                val href = element.attr("href")
//                                "https://naruto-base.su$href"
//                            }
//                        if (urls.isNotEmpty()) {
//
//                            for (chatId in s.value) {
//                            }
//                                sendImages(chatId, urls)
//                            newLatest = i.first
//                        }
//                    }
//                    if (latest != newLatest) {
//                        TODO("Notify bot")
//                        sub.lastVersion = newLatest
//                        storage.save(sub)
//                    }
//                }
//            }
//        }
//    }
//}