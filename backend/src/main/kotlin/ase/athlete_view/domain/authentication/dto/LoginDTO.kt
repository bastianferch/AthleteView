package ase.athlete_view.domain.authentication.dto

import java.util.*

class LoginDTO(
    val email: String,
    val password: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val o: LoginDTO = other as LoginDTO
        return Objects.equals(this.email, o.email) && Objects.equals(this.password, o.password)
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}
