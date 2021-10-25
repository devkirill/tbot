package com.project.tbot.parser.model

class SendModelContent(
    var title: String,
    var link: String,
    var description: String,
    var images: List<String>
) {
    companion object {
        inline operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder(
        var title: String = "",
        var link: String = "",
        var description: String = "",
        var images: List<String> = listOf()
    ) {
        fun build(): SendModelContent {
            return SendModelContent(title, link, description, images)
        }
    }
}