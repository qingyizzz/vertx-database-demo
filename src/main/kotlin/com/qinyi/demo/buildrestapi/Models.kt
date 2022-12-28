package com.qinyi.demo.buildrestapi

import java.lang.reflect.Constructor
import java.time.LocalDateTime
import java.util.*

data class Player(
  var playerId: Long,
  var teamId: Long,
  var playerName: String,
  var height: Float
){
  constructor() : this(-1,-1,"",0.0f)
}

data class Post(
  var id: UUID? = null,
  var title: String,
  var content: String,
  var createdAt: LocalDateTime? = LocalDateTime.now()
)

data class CreatePostCommand(
  var title: String,
  var content: String
){
  constructor(): this("","")
}
