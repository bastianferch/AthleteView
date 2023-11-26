package ase.athlete_view.domain.token.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "token_type")
abstract class Token(
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid", strategy = "uuid4")
    open var uuid: UUID? = null,

    @Column(nullable = false, updatable = false)
    open var expiryDate: Date,

    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    open var user: User
) {

    val isExpired: Boolean
        get() = expiryDate.before(Date())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        other as Token

        return uuid != null && uuid == other.uuid
    }

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , expiryDate = $expiryDate )"
    }

    override fun hashCode(): Int {
        var result = uuid?.hashCode() ?: 0
        result = 31 * result + expiryDate.hashCode()
        return result
    }
}

