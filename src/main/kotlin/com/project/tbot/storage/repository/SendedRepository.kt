package com.project.tbot.storage.repository

import com.project.tbot.storage.model.Sended
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SendedRepository : JpaRepository<Sended, Long> {
    @Query("SELECT s FROM Sended s WHERE s.chatId = :chatId and s.guid = :guid")
    fun find(chatId: Long, guid: String): List<Sended>
}