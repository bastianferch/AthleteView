package ase.athlete_view.domain.time_constraint.pojo.entity

import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="time_constraints")
open class TimeConstraint(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        open val id: Long?,
        open val isBlacklist: Boolean,

        open val title: String,

        @ManyToOne
        open val user: User
) {
        open fun toDto(): TimeConstraintDto{
                return TimeConstraintDto(id, isBlacklist, title, user)
        }

        override fun toString(): String {
                return "TimeConstraint(id=$id, isBlacklist=$isBlacklist, title='$title', user=$user)"
        }


}
