package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.common.exception.fitimport.DuplicateFitFileException
import ase.athlete_view.domain.activity.pojo.dto.FitData
import com.mongodb.BasicDBObject
import com.mongodb.client.gridfs.model.GridFSFile
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Repository
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.MessageDigest


@Repository
class FitDataRepositoryImpl(
    private val gridFsTemplate: GridFsTemplate,
    private val gridFsOperations: GridFsOperations
): FitDataRepository {
    private val log = KotlinLogging.logger {}

    private fun getSha256Digest(data: ByteArray): String {
        log.trace { "P | getSha256Digest()" }
        val messageDigest = MessageDigest.getInstance("sha256")
        messageDigest.update(data)
        val digest = messageDigest.digest()
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    override fun saveFitData(data: InputStream, filename: String): String {
        log.trace { "P | saveFitData($filename)" }
        val bin = BufferedInputStream(data)
        if (checkIfFileExists(bin)) {
            throw DuplicateFitFileException("File already in-store!")
        }

        val metadata = BasicDBObject()

        bin.mark(Integer.MAX_VALUE)
        metadata.append("hash", getSha256Digest(bin.readAllBytes()))
        bin.reset()
        val id: ObjectId = gridFsTemplate.store(data,filename, metadata)
        return id.toString()
    }

    override fun getFitData(id: String): FitData {
        log.trace { "P | getFitData($id)" }
        val file: GridFSFile = gridFsTemplate.findOne(Query(Criteria.where("_id").`is`(id)))
        return FitData(
            id,
            gridFsOperations.getResource(file).inputStream
        )
    }

    private fun checkIfFileExists(data: BufferedInputStream): Boolean {
        log.trace { "P | checkIfFileExists()" }
        data.mark(Integer.MAX_VALUE)
        val hashValue = getSha256Digest(data.readAllBytes())
        data.reset()
        log.debug { "Checking if file with hash $hashValue exists" }
        val file = gridFsTemplate.find(Query(Criteria.where("metadata.hash").`is`(hashValue))).firstOrNull()
        return file !== null
    }
}