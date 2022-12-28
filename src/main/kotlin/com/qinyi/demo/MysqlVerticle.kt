package com.qinyi.demo

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple


class MysqlVerticle : AbstractVerticle() {
  val connectOptions = MySQLConnectOptions()
    .setPort(3306)
    .setHost("127.0.0.1")
    .setDatabase("nba")
    .setUser("root")
    .setPassword("123456")

  // Pool options
  val poolOptions = PoolOptions()
    .setMaxSize(5)

  // 连接池
  val pool = MySQLPool.pool(vertx, connectOptions, poolOptions)

  fun selectfromdb() {
    // A simple query
    pool.query("SELECT * FROM player WHERE player_id=10001")
      .execute { ar: AsyncResult<RowSet<Row?>> ->
        if (ar.succeeded()) {
          val result: RowSet<Row?> = ar.result()
          println("Got " + result.size() + " rows ")
          for (r in result) {
            println(r)
          }
        } else {
          println("Failure: " + ar.cause().message)
        }
      }
  }

  fun deleteFromDb() {
    // 删
    pool.preparedQuery("DELETE FROM player where player_id = ?")
      .execute(
        Tuple.of(10038)
      ) { ar: AsyncResult<RowSet<Row?>> ->
        if (ar.succeeded()) {
          val rows = ar.result()
          println(rows.rowCount())
        } else {
          println("Failure: " + ar.cause().message)
        }
      }
  }

  fun updatefromDb() {
    // 改
    val result = pool.preparedQuery("UPDATE player SET height = ? where player_id = ?")
//      .executeAwait()
//    result.forEach {
      println()
//    }
  }

  fun insertInToDb() {
    // 增
    pool.preparedQuery("INSERT INTO player VALUES (?, ?,?,?)")
      .execute(
        Tuple.of(null, 999, "james",2.07)
      ) { ar: AsyncResult<RowSet<Row?>> ->
        if (ar.succeeded()) {
          val rows = ar.result()
          println(rows.rowCount())
        } else {
          println("Failure: " + ar.cause().message)
        }
      }
  }
}

fun main(args: Array<String>) {
//  MysqlVerticle().selectfromdb()
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MysqlVerticle::class.java.canonicalName)

}
