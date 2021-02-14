package com.project.tbot.parser.model

data class Post(
    val guid: String?,
    val link: String,
    val title: String,
    val description: String,
    val images: List<String>,
    val category: List<String>
) {
    companion object {
        inline operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder(
        var guid: String? = null,
        var link: String = "",
        var title: String = "",
        var description: String = "",
        var images: List<String> = listOf(),
        var category: List<String> = listOf()
    ) {
        fun build(): Post = Post(
            guid = guid,
            link = link,
            title = title,
            description = description,
            images = images,
            category = category
        )
    }
}