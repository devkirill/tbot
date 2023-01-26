package com.project.tbot.storage

import com.project.tbot.storage.model.Sended
import com.project.tbot.storage.model.Subscribe
import com.project.tbot.storage.repository.SendedRepository
import com.project.tbot.storage.repository.SubscribeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Storage {
    @Autowired
    lateinit var sendedRepository: SendedRepository

    @Autowired
    lateinit var subscribeRepository: SubscribeRepository

    fun <T : Any> save(obj: T) {
        when (obj) {
            is Sended -> {
                sendedRepository.save(obj)
            }
            is Subscribe -> {
                subscribeRepository.save(obj)
            }
            else -> {
                TODO("not supported")
            }
        }
    }

    fun alreadySend(sended: Sended): Boolean {
        return sendedRepository.find(sended.chatId, sended.guid).isNotEmpty()
    }

    fun getAllSubscribed(): List<Subscribe> {
        return subscribeRepository.findAll()
    }

    fun <T : Any> remove(obj: T) {
        when (obj) {
            is Sended -> {
                sendedRepository.deleteById(obj.id ?: return)
            }

            is Subscribe -> {
                subscribeRepository.deleteById(obj.id ?: return)
            }

            else -> {
                TODO("not supported")
            }
        }
    }
}
