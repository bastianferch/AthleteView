package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.Trainer

class TrainerDTO (
    val id: Long?,
    var email: String,
    var name: String,
    var country: String?,
    var zip: String?,
    var code: String,
    var athletes: List<AthleteDTO> = ArrayList()
){
    fun toEntity(): Trainer {
        val trainer = Trainer(
            id,
            email,
            name,
            "",
            country,
            zip,
            code
        )
        val athletes = athletes.map { it.toEntity() }
        trainer.athletes = athletes
        return trainer
    }
}