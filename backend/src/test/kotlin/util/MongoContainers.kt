package util

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

object MongoContainers {
    private const val IMAGE_NAME = "mongo:7.0"
    private const val IMAGE_NAME_PROPERTY = "mongo.default.image.name"

    val defaultContainer: MongoDBContainer
        get() = MongoDBContainer(
            DockerImageName.parse(System.getProperty(IMAGE_NAME_PROPERTY, IMAGE_NAME))
        ).withReuse(true)
}