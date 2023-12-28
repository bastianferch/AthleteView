package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.Trainer

class TrainerDTO(
    id: Long?,
    email: String,
    name: String,
    var country: String?,
    var zip: String?,
    var code: String,
    token: String?,
    userType: String = "trainer",
    var athletes: List<AthleteDTO> = ArrayList(),
    var unacceptedAthletes: List<AthleteDTO> = ArrayList(),
): UserDTO(
    id,name,email, null, token, userType
){
    fun toEntity(): Trainer {
        val trainer = Trainer(
            id,
            email,
            name,
            "",
            country,
            zip,
            code,
            mutableSetOf(),
            mutableSetOf()
        )
        val athletes = athletes.map { it.toEntity() }.toMutableSet()
        trainer.athletes = athletes
        return trainer
    }
}
