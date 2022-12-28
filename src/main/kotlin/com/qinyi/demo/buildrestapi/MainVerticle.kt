package com.qinyi.demo.buildrestapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class MainVerticle : CoroutineVerticle() {
  companion object {
    private val LOGGER = Logger.getLogger(MainVerticle::class.java.name)

    /**
     * Configure logging from logging.properties file.
     * When using custom JUL logging properties, named it to vertx-default-jul-logging.properties
     * or set java.util.logging.config.file system property to locate the properties file.
     */
    @Throws(IOException::class)
    private fun setupLogging() {
      MainVerticle::class.java.getResourceAsStream("/logging.properties")
        .use { f -> LogManager.getLogManager().readConfiguration(f) }
    }

    init {
      LOGGER.info("Customizing the built-in jackson ObjectMapper...")
      val objectMapper = DatabindCodec.mapper()
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
      objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
      val module = JavaTimeModule()
      objectMapper.registerModule(module)
    }
  }

  //  @Throws(Exception::class)
  override suspend fun start() {
    LOGGER.log(Level.INFO, "Starting HTTP server...")
    //setupLogging();

    //Create a PgPool instance
//    val pgPool = pgPool()
    val mysqlPool = mysqlPool()

    //Creating PostRepository
//    val postRepository = PostRepository(pgPool)
    val postRepository = PlayerRepository(mysqlPool)

    //Creating PostHandler
    val postHandlers = PlayerHandler(postRepository)

    // Initializing the sample data
//    val initializer = DataInitializer(pgPool)
//    initializer.run()

    // Configure routes
    val router = routes(postHandlers)

    // Create the HTTP server
    vertx.createHttpServer() // Handle every request using the router
      .requestHandler(router) // Start listening
      .listen(8008) // Print the port
      .onComplete {
        println("HTTP server started on port " + it.result().actualPort())
      }
      .await()
  }

  override suspend fun stop() {
    super.stop()
  }

  //create routes
  private fun routes(handlers: PlayerHandler): Router {

    // Create a Router
    val router = Router.router(vertx)
    // register BodyHandler globally.
    //router.route().handler(BodyHandler.create());
    router.get("/player")
      .produces("application/json")
      .coroutineHandler { handlers.all(it) }

    router.post("/player")
      .consumes("application/json")
      .handler(BodyHandler.create())
      .coroutineHandler { handlers.save(it) }

    router.get("/player/:id")
      .produces("application/json")
      .coroutineHandler { handlers.getById(it) }

    router.put("/player/:id")
      .consumes("application/json")
      .handler(BodyHandler.create())
      .coroutineHandler { handlers.update(it) }

    router.delete("/player/:id")
      .coroutineHandler { handlers.delete(it) }

    router.route().failureHandler {
      if (it.failure() is PlayerNotFoundException) {
        it.response()
          .setStatusCode(404)
          .end(
            json {// an example using JSON DSL
              obj(
                "message" to "${it.failure().message}",
                "code" to "not_found"
              )
            }.toString()
          )
      }
    }

    router.get("/hello").handler { it.response().end("Hello from my route") }

    return router
  }

  private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) = handler {
    launch(it.vertx().dispatcher()) {
      try {
        fn(it)
      } catch (e: Exception) {
        it.fail(e)
      }
    }
  }

  private fun mysqlPool(): MySQLPool {
    val connectOptions = MySQLConnectOptions()
      .setPort(3306)
      .setHost("127.0.0.1")
      .setDatabase("nba")
      .setUser("root")
      .setPassword("123456")
//      .setCharset("utf-8")

    // Pool options
    val poolOptions = PoolOptions().setMaxSize(5)

    // 连接池
    return MySQLPool.pool(vertx, connectOptions, poolOptions)
  }
}

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(com.qinyi.demo.buildrestapi.MainVerticle::class.java.canonicalName)
}
