package ase.athlete_view.util.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.StateID
import de.flapdoodle.reverse.TransitionWalker
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration


@Configuration
class FlapdoodleConfig : AbstractMongoClientConfiguration() {
    private lateinit var walker: TransitionWalker.ReachedState<RunningMongodProcess>

    init {
        startDb()
    }

    private final fun startDb() {
        val transitions = Mongod.instance().transitions(Version.V7_0_0)
        walker = transitions.walker().initState(StateID.of(RunningMongodProcess::class.java))
    }

    @PreDestroy
    fun teardown() {
        walker.close()
    }

    override fun mongoClient(): MongoClient {
        return MongoClients.create("mongodb://${walker.current().serverAddress}")
    }

    override fun getDatabaseName(): String {
        return "athlete_view_testdb"
    }
}
