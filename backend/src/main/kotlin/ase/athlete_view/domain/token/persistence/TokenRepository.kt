package ase.athlete_view.domain.token.persistence

import ase.athlete_view.domain.token.pojo.entity.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TokenRepository : JpaRepository<Token, UUID> {
    fun findByUuid(uuid: UUID): Optional<Token>
    @Modifying
    @Query("DELETE Token WHERE expiryDate < current_date")
    fun deleteExpired()

    @Modifying
    @Query("DELETE EmailConfirmationToken t WHERE t.user.id = ?1")
    fun deleteAllRegistrationTokensByUser(userId: Long)

    @Modifying
    @Query("DELETE PasswordResetToken t WHERE t.user.id = ?1")
    fun deleteAllPasswordResetTokensByUser(userId: Long)
}