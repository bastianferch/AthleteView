package util.mongo

import org.testcontainers.containers.MongoDBContainer

object MongoContainerSingleton {
    val instance: MongoDBContainer by lazy { startMongoContainer() }
    private fun startMongoContainer(): MongoDBContainer =
        MongoDBContainer("mongo:7.0")
            .withReuse(true)
            .apply { start() }
}