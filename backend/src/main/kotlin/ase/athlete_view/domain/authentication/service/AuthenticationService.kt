package ase.athlete_view.domain.authentication.service

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface AuthenticationService {
    fun registerUser(user: User): User;
    fun authenticateUser(loginDTO: LoginDTO): User;
}
