package com.project.tbot.storage.service

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.project.tbot.md5
import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Storage(@Value("\${storage.address:http://192.168.9.111:9200}") val address: String) {
    fun <T : Any> save(obj: T) {
        val name = obj::class.simpleName!!.toLowerCase()
        val content = Gson().toJson(obj)
        runBlocking {
            val client = HttpClient()

            val uid = getUid(obj)

            val result: String = client.put("$address/$name/type/$uid") {
                contentType(ContentType.Application.Json)
                body = content
            }

            println(result)

            client.close()
        }
    }

//    fun <T> contains(clazz: Class<T>): List<T> {
//
//    }

    fun <T> getAll(clazz: Class<T>): List<T> {
        val className = clazz.simpleName.toLowerCase()
        val result = mutableListOf<T>()
        runBlocking {
            val client = HttpClient()

            val response: String = client.get("$address/$className/type/_search") {
                contentType(ContentType.Application.Json)
                body = "{\"size\": 1000}"
            }
            val list = JsonParser().parse(response).asJsonObject.getAsJsonObject("hits").getAsJsonArray("hits")
            for (t in list) {
                result.add(Gson().fromJson(t.asJsonObject.getAsJsonObject("_source"), clazz))
            }

            client.close()
        }
        return result
    }

    final inline fun <reified T> getAll() = getAll(T::class.java)

    fun <T : Any> getUid(obj: T): String = when (obj) {
        is Sended -> "${obj.chatId}/${obj.guid}".md5
        is Subscribe -> "${obj.chatId}/${obj.rss}".md5
        else -> throw IllegalStateException("")
    }
}
