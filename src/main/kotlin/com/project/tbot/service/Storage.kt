package com.project.tbot.service

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.project.tbot.model.RootObject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class Storage(val address: String = "http://localhost:9200") {
    fun <T : RootObject> save(obj: T) {
        val name = obj::class.simpleName!!.toLowerCase()
        val content = Gson().toJson(obj)
        runBlocking {
            val client = HttpClient()

            val result: String = client.put("$address/$name/type/${obj.id}") {
                contentType(ContentType.Application.Json)
                body = content
            }

            println(result)

            client.close()
        }
    }

    fun <T> getAll(clazz: Class<T>): List<T> {
        val className = clazz.simpleName.toLowerCase()
        val result = mutableListOf<T>()
        runBlocking {
            val client = HttpClient()

            val response: String = client.get("$address/$className/type/_search") {
                contentType(ContentType.Application.Json)
                body = "{}"
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
}
