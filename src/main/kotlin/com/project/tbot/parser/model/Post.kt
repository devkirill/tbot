package com.project.tbot.parser.model

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

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
        fun build(): Post {
            if (description.contains(Regex("</?\\w+(\\s*/)?>"))) {
                val body = Jsoup.parse(description).body()
                fun recursive(root: Element): String {
                    var begin = ""
                    var end = ""
                    when (root.tag().name.toLowerCase()) {
                        "img" -> images += root.attr("src")
                        "p" -> {
                            begin += "\n\n"
                            end += "\n\n"
                        }
                        "br" -> begin += "\n\n"
                        "a" -> {
                            val url = root.attr("href")
                            when {
                                link == url -> {
                                }
                                link.isBlank() -> link = url
                                root.text().isNotBlank() -> {
                                    begin += "["
                                    end += "]($link)"
                                }
                            }
                        }
                        "b", "strong" -> {
                            begin += "**"
                            end += "**"
                        }
                        "i", "em" -> {
                            begin += "_"
                            end += "_"
                        }
                        "u", "ins" -> {
                            begin += "__"
                            end += "__"
                        }
                        "s", "strike", "del" -> {
                            begin += "~"
                            end += "~"
                        }
                    }
                    for (child in root.childNodes()) {
                        begin += when (child) {
                            is TextNode -> child.text().replace("\n", " ").trim()
                            is Element -> recursive(child)
                            else -> ""
                        }
                    }
                    return begin + end
                }
                description =
                    recursive(body).replace(Regex("\n\\s+\n"), "\n").replace(Regex("\n+"), "\n").trim(' ', '\n')
            }

            return Post(
                guid = guid,
                link = link,
                title = title,
                description = description,
                images = images,
                category = category
            )
        }
    }
}