package com.project.tbot.storage.model

import javax.persistence.*

@Entity
@Table(name = "Sended")
data class Sended(
    @Id
    @GeneratedValue
    @Column
    var id: Long? = null,
    @Column
    var chatId: Long,
    @Column
    var guid: String
)