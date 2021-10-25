package com.project.tbot.storage.model

import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "Subscribe")
data class Subscribe(
    @Id
    @GeneratedValue
    @Column
    var id: Long? = null,
    @Column
    @NotNull
    var chatId: Long,
    @Column
    var type: String,
    @Column
    @NotEmpty
    var rss: String
//    @Column
//    var weight: Long
)