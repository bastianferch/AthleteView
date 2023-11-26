package util.mongo

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class MongoContainerInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val addedProperties = listOf(
            "spring.data.mongodb.uri=${MongoContainerSingleton.instance.replicaSetUrl}"
        )
        TestPropertyValues.of(addedProperties).applyTo(applicationContext.environment)
    }
}
