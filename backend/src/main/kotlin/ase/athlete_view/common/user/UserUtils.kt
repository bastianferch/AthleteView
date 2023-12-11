package ase.athlete_view.common.user

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import org.springframework.security.core.context.SecurityContextHolder

class UserUtils() {
    companion object {
        fun currentUser(): UserDTO {
            return SecurityContextHolder.getContext().authentication.principal as UserDTO
        }
    }
}