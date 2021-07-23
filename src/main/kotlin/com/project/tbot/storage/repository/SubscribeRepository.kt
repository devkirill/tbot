package com.project.tbot.storage.repository

import com.project.tbot.storage.model.Subscribe
import org.springframework.data.jpa.repository.JpaRepository

interface SubscribeRepository : JpaRepository<Subscribe, Long>