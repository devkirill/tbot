package com.project.tbot.model

import com.google.gson.Gson
import com.project.tbot.md5
import java.io.Serializable

interface RootObject : Serializable {
    val id: String
        get() = this.toJson().md5()

    fun toJson() = Gson().toJson(this)
}