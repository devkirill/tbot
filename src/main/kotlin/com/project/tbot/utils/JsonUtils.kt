package com.project.tbot.utils

import com.google.gson.Gson

val GSON = Gson()

inline fun <reified T> String.fromJson(): T = GSON.fromJson(this, T::class.java)

fun Any.toJson() = GSON.toJson(this)