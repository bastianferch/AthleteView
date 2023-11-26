package util.mongo

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@Target(AnnotationTarget.CLASS)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = [MongoContainerInitializer::class])
annotation class ContainerizedMongoTest