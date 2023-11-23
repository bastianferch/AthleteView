package ase.athlete_view.util

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.mongo.types.DatabaseDir
import de.flapdoodle.reverse.TransitionWalker.ReachedState
import de.flapdoodle.reverse.transitions.Start
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.file.Files


class EmbeddedMongo : Closeable {
    @Bean
    @Throws(Exception::class)
    fun mongoTemplate(): MongoTemplate? {
        if (mongoTemplate == null) {
            val port = 17027

            //Store database in temp folder
            //More info: https://stackoverflow.com/a/1924576/3710490
            val tmpdir = System.getProperty("java.io.tmpdir")
            val databaseDir = File(tmpdir, "database")
            if (!databaseDir.exists()) {
                Files.createDirectory(databaseDir.toPath())
            }
            val mongodWithoutAuth: ImmutableMongod = Mongod.builder()
                .net(Start.to(Net::class.java).initializedWith(Net.defaults().withPort(port)))
                .databaseDir(
                    Start.to(DatabaseDir::class.java).initializedWith(DatabaseDir.of(databaseDir.toPath()))
                ).build()
            mongod = mongodWithoutAuth.start(Version.Main.V6_0)
            val address: ServerAddress = (mongod as ReachedState<RunningMongodProcess>).current().serverAddress
            client = MongoClients.create(String.format(CONNECTION_STRING, address.host, address.port))
            mongoTemplate = MongoTemplate(client!!, "test")
        }
        return mongoTemplate
    }

    @Throws(IOException::class)
    override fun close() {
        if (mongod!!.current().isAlive) {
            client!!.close()
            mongod!!.current().stop()
            mongoTemplate = null
        }
    }

    companion object {
        private const val CONNECTION_STRING = "mongodb://%s:%d"
        private var mongod: ReachedState<RunningMongodProcess>? = null
        private var client: MongoClient? = null
        private var mongoTemplate: MongoTemplate? = null
    }
}
