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

    override fun toString(): String {
        return "TrainerRegistrationDTO(email=$email, name=$name, country=$country, zip=$zip)"
    }

    fun toEntity(code: String): Trainer {
        return Trainer(null, email!!, name!!, password!!, country, zip, code, mutableSetOf(), mutableSetOf())
    }
}
