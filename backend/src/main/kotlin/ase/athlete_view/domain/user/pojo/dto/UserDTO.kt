package ase.athlete_view.domain.user.pojo.dto

open class UserDTO(
    open var id: Long? = null,
    open var name: String,
    open var email: String,
    open var password: String?,
    open var token: String?,
    open var userType: String
) {
    override fun toString(): String {
        return "UserDto(id=$id, name='$name', email='$email')"
    }
}
