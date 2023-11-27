package ase.athlete_view.domain.token.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.util.*

@DiscriminatorValue("reset_password")
@Entity
class PasswordResetToken(
    uuid: UUID? = null,
    expiryDate: Date,
    user: User
): Token(uuid, expiryDate, user)