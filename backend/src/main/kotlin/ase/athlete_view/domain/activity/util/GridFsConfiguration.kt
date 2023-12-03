package ase.athlete_view.domain.activity.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["ase.athlete_view.domain.activity"])
abstract class GridFsConfiguration: AbstractMongoClientConfiguration() {
    @Autowired
    private lateinit var mappingMongoConverter: MappingMongoConverter

    @Bean
    fun gridFsTemplate(): GridFsTemplate {
        return GridFsTemplate(mongoDbFactory(), mappingMongoConverter)
    }
}