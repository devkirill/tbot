package com.project.tbot.parser.model

data class Feed(
    val title: String,
    val link: String,
    val description: String,
    val image: String,
    val posts: List<Post>
) {
    companion object {
        inline operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder(
        var title: String = "",
        var link: String = "",
        var description: String = "",
        var image: String = "",
        var posts: List<Post> = listOf()
    ) {
        fun build(): Feed = Feed(title, link, description, image, posts)
    }
}