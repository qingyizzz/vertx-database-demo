package com.qinyi.demo.buildrestapi

import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import java.util.logging.Level
import java.util.logging.Logger

class PlayerHandler(val player: PlayerRepository) {
  suspend fun all(rc: RoutingContext) {
    val data = player.findAll()
    rc.response().end(Json.encode(data)).await()
  }

  suspend fun getById(rc: RoutingContext) {
    val params = rc.pathParams()
    val id = params["id"]
    val data = player.findById(id?.toLong() ?: -1)
    if (data != null) {
      rc.response().end(Json.encode(data)).await()
    } else {
      rc.fail(404, PlayerNotFoundException(id?.toLong() ?: -1))
    }
  }

  suspend fun save(rc: RoutingContext) {
    val body = rc.bodyAsJson
    LOGGER.log(Level.INFO, "request body: {0}", body)
    val (playerId, teamId, playerName, height) = body.mapTo(Player::class.java)
    val rows = player.save(Player(playerId = playerId, teamId = teamId, playerName = playerName, height = height))
    if (rows > 0) {
      rc.response().setStatusCode(201)
        .end()
        .await()
    } else {
      rc.fail(500)
    }
  }

  suspend fun update(rc: RoutingContext) {
    val params = rc.pathParams()
    val id = params["id"]
    val body = rc.bodyAsJson
    LOGGER.log(Level.INFO, "\npath param id: {0}\nrequest body: {1}", arrayOf(id, body))
    var (playerId, teamId, playerName, height) = body.mapTo(Player::class.java)
    var existing:Player? = player.findById(id?.toLong() ?: -1)
    if (existing != null) {
      player.update(Player(id?.toLong() ?: -1, teamId, playerName, height))
      rc.response().setStatusCode(204).end().await()
    } else {
      rc.fail(404, PlayerNotFoundException(id?.toLong() ?: -1))
    }
  }

  suspend fun delete(rc: RoutingContext) {
    val params = rc.pathParams()
    val id = params["id"]
    var existing:Player? = player.findById(id?.toLong() ?: -1)
    if (existing != null) {
      player.deleteById(id?.toLong() ?: -1)
      rc.response().setStatusCode(204).end().await()
    } else {
      rc.fail(404, PlayerNotFoundException(id?.toLong() ?: -1))
    }
  }

  companion object {
    private val LOGGER = Logger.getLogger(PlayerHandler::class.java.simpleName)
  }
}
