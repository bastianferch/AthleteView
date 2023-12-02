package ase.athlete_view.domain.user.pojo.dto

class UserDTO(
    var id: Long? = null,
    val name: String,
    val email: String,
    val password: String?,
    var token: String?,
) {
    override fun toString(): String {
        return "UserDto(id=$id, name='$name', email='$email')"
    }
}
