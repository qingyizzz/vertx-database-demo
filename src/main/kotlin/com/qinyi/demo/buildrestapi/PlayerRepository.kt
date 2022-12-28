package com.qinyi.demo.buildrestapi

import io.vertx.mysqlclient.MySQLPool
import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.util.logging.Logger
import java.util.stream.StreamSupport
import kotlin.streams.toList

class PlayerRepository(private val client: MySQLPool) {

  suspend fun findAll() = client.query("SELECT * FROM player ORDER BY player_id ASC")
    .execute()
    .map { rs: RowSet<Row?> ->
      StreamSupport.stream(rs.spliterator(), false)
        .map { rowToPlayerFun(it!!) }
        .toList()
    }
    .await()


  suspend fun findById(id: Long) = client.preparedQuery("SELECT * FROM player WHERE player_id = ?")
    .execute(Tuple.of(id))
    .map { it.iterator() }
    .map {
      if (it.hasNext()) rowToPlayerFun(it.next());
      else null
    }
    .await()


  suspend fun save(data: Player) = client.preparedQuery("INSERT INTO player VALUES (?, ?, ?, ?)")
    .execute(Tuple.of(data.playerId, data.teamId, data.playerName, data.height))
    .map { it.rowCount() }
    .await()


  fun saveAll(data: List<Post>): Future<Int> {
    val tuples = data.map { Tuple.of(it.title, it.content) }

    return client.preparedQuery("INSERT INTO posts (title, content) VALUES ($1, $2)")
      .executeBatch(tuples)
      .map { it.rowCount() }
  }

  suspend fun update(data: Player) = client.preparedQuery("UPDATE player SET team_id=?, player_name=?, height=? WHERE player_id=?")
    .execute(Tuple.of(data.teamId, data.playerName, data.height, data.playerId))
    .map { it.rowCount() }
    .await()



  fun deleteAll() = client.query("DELETE FROM posts").execute()
    .map { it.rowCount() }


  suspend fun deleteById(id: Long) = client.preparedQuery("DELETE FROM player WHERE player_id=?").execute(Tuple.of(id))
    .map { it.rowCount() }
    .await()

  companion object {
    private val LOGGER = Logger.getLogger(PlayerRepository::class.java.name)
    val mapFun: (Row) -> Post = { row: Row ->
      Post(
        row.getUUID("id"),
        row.getString("title"),
        row.getString("content"),
        row.getLocalDateTime("created_at")
      )
    }

    val rowToPlayerFun: (Row) -> Player = { row: Row ->
      Player(
        row.getLong("player_id"),
        row.getLong("team_id"),
        row.getString("player_name"),
        row.getFloat("height")
      )
    }

  }
}
