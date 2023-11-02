package ase.athlete_view.domain.user.pojo.dto

class UserDto(
    var id: Long? = null,
    val name: String,
    val email: String,
    val password: String?,
    val token: String?,
)
