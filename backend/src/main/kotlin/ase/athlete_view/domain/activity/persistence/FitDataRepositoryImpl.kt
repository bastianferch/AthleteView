package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.dto.FitData
import com.mongodb.client.gridfs.model.GridFSFile
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile


//abstract class FitDataRepository : MongoRepository<MultipartFile, String> {}


@Repository
class FitDataRepositoryImpl(
    private val gridFsTemplate: GridFsTemplate,
    private val gridFsOperations: GridFsOperations
): FitDataRepository {
    override fun saveFitData(data: MultipartFile): String {
        val id: ObjectId = gridFsTemplate.store(data.inputStream, data.name)
        return id.toString()
//        return ""
    }

    fun getFitData(id: String): FitData? {
        val file: GridFSFile = gridFsTemplate.findOne(Query(Criteria.where("_id").`is`(id)))
        return FitData(
            id,
            gridFsOperations.getResource(file).inputStream
        )
//        return null
    }
}