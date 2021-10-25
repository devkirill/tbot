//package com.project.tbot.storage.repository
//
//import com.project.tbot.parser.model.Post
//import com.project.tbot.storage.model.PostQueue
//import com.project.tbot.storage.model.Sended
//import org.springframework.data.jpa.repository.JpaRepository
//import org.springframework.data.jpa.repository.Query
//
//interface PostQueueRepository : JpaRepository<PostQueue, Long> {
//    @Query("SELECT pq FROM PostQueue pq WHERE pq.chatId in ( SELECT d.chatId FROM ( SELECT pq2.chatId, sum(pq2.weight) as weight FROM PostQueue pq2 GROUP BY pq2.chatId ) d WHERE d.weight >= 100 ) and pq.chatId = :chatId")
//    fun find(chatId: Long): List<PostQueue>
//}