package com.project.tbot.utils

import java.io.InputStream
import java.nio.charset.StandardCharsets

fun String(inputStream: InputStream) = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
