package com.project.tbot.storage.model

import javax.persistence.*

@Entity
@Table(name = "Subscribe")
data class Subscribe(
    @Id
    @GeneratedValue
    @Column
    var id: Long? = null,
    @Column
    var chatId: Long,
    @Column
    var rss: String
)