package ase.athlete_view.domain.authentication.dto

import ase.athlete_view.domain.user.pojo.entity.Trainer

data class TrainerRegistrationDTO
    (
    override var email: String?,
    override var name: String?,
    override var password: String?,
    override var country: String?,
    override var zip: String?
) :
    RegistrationDTO(email, name, password, country, zip){
}

fun TrainerRegistrationDTO.toEntity(code: String): Trainer {
    return Trainer(null, email!!, name!!, password!!, country, zip, code, mutableSetOf(), mutableSetOf())
}