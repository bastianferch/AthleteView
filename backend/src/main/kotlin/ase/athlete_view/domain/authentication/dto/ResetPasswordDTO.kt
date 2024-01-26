package ase.athlete_view.domain.authentication.dto

import java.util.UUID

data class ResetPasswordDTO(
    val password: String,
    val token: UUID
) {
    override fun toString(): String {
        return "ResetPasswordDTO(token=$token)"
    }
}