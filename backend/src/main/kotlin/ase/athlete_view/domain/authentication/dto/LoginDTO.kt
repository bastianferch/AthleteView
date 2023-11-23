package ase.athlete_view.domain.authentication.dto

class LoginDTO(
    val email: String,
    val password: String,
) {
    override fun toString(): String {
        return "{ 'email': '${email}', 'password': '${password}'}"
    }
}
