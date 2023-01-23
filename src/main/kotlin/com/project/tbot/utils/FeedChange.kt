package com.project.tbot.utils

import com.project.rss.model.Item
import com.project.tbot.parser.model.Feed
import com.project.tbot.parser.model.Post

fun com.project.rss.model.Feed.toFeed(): Feed =
    Feed(
        this.title ?: "",
        this.link ?: "",
        this.description ?: "",
        this.image?.url ?: "",
        this.item.map { it.toPost() }
    )

fun Item.toPost(): Post =
    Post(
        this.guid,
        this.link ?: "",
        this.title ?: "",
        this.description ?: "",
        this.img,
        listOf(),
        this.file
    )