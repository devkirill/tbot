package com.project.tbot.storage

import com.project.tbot.md5
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import com.project.tbot.storage.repository.SendedRepository
import com.project.tbot.storage.repository.SubscribeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Storage {
    @Autowired
    lateinit var sendedRepository: SendedRepository

    @Autowired
    lateinit var subscribeRepository: SubscribeRepository

    fun <T : Any> save(obj: T) {
        when (obj) {
            is Sended -> {
                sendedRepository.save(obj)
            }
            is Subscribe -> {
                subscribeRepository.save(obj)
            }
            else -> {
                TODO("not supported")
            }
        }
    }

//    fun <T> getAll(clazz: Class<T>): List<T> {
//        val className = clazz.simpleName.toLowerCase()
//        val result = mutableListOf<T>()
//        runBlocking {
//            val client = HttpClient()
//
//            val response: String = client.get("$address/$className/type/_search") {
//                contentType(ContentType.Application.Json)
//                body = "{\"size\": 10000}"
//            }
//            val list = JsonParser().parse(response).asJsonObject.getAsJsonObject("hits").getAsJsonArray("hits")
//            for (t in list) {
//                result.add(Gson().fromJson(t.asJsonObject.getAsJsonObject("_source"), clazz))
//            }
//
//            client.close()
//        }
//        return result
//    }

    fun alreadySend(sended: Sended): Boolean {
        return sendedRepository.find(sended.chatId, sended.guid).isNotEmpty()
    }

    fun getAllSubscribed(): List<Subscribe> {
        return subscribeRepository.findAll()
    }

    fun <T : Any> getUid(obj: T): String = when (obj) {
        is Sended -> "${obj.chatId}/${obj.guid}".md5
        is Subscribe -> "${obj.chatId}/${obj.rss}".md5
        else -> throw IllegalStateException("")
    }
}
