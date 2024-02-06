/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
import java.io.ByteArrayInputStream
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

    override fun saveFitData(data: InputStream, filename: String, uid: Long): String {
        log.trace { "P | saveFitData($filename)" }
        val byteData = data.readAllBytes()
        if (checkIfFileExists(byteData, uid, filename)) {
            throw DuplicateFitFileException("File already in-store!")
        }

        val metadata = BasicDBObject()

        metadata.append("hash", getSha256Digest(byteData))
        metadata.append("uid", uid)
        val id: ObjectId = gridFsTemplate.store(ByteArrayInputStream(byteData), filename, metadata)
        return id.toString()
    }

    override fun getFitData(id: String): FitData {
        log.trace { "P | getFitData($id)" }
        val file: GridFSFile = gridFsTemplate.findOne(Query(Criteria.where("_id").`is`(id)))
        return FitData(
            id,
            ByteArrayInputStream(gridFsOperations.getResource(file).contentAsByteArray)
        )
    }

    private fun checkIfFileExists(data: ByteArray, uid: Long, filename: String): Boolean {
        log.trace { "P | checkIfFileExists()" }
        val hashValue = getSha256Digest(data)
        log.debug { "Checking if file with hash $hashValue exists" }
        val file = gridFsTemplate.find(Query(
                Criteria.where("metadata.hash").`is`(hashValue).andOperator(Criteria.where("metadata.uid").`is`(uid))
        )).firstOrNull()
        if (file === null){
            return false
        }
        return file.filename == filename
    }
}
