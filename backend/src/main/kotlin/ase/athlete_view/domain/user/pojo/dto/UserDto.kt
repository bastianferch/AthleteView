package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User

class UserDto(
    var id: Long? = null,
    val name: String,
    val email: String,
    val password: String?,
    var token: String?,
) {
    fun toEntity(): User {
        return Trainer(id, email, name, "", "", "","")
    }

    override fun toString(): String {
        return "UserDto(id=$id, name='$name', email='$email')"
    }
}
