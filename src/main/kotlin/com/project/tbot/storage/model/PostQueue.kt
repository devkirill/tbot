//package com.project.tbot.storage.model
//
//import com.project.tbot.parser.model.SendModelContent
//import com.project.tbot.utils.fromJson
//import com.project.tbot.utils.toJson
//import javax.persistence.*
//
//@Entity
//@Table(name = "Queue")
//data class PostQueue(
//    @Id
//    @GeneratedValue
//    @Column
//    var id: Long? = null,
//    @Column
//    var chatId: Long,
//    @Column
//    var weight: Long,
//    @Column
//    @Lob
//    var post: String = ""
//) {
//    var content: SendModelContent
//        get() = post.fromJson()
//        set(value) {
//            post = value.toJson()
//        }
//}