package ase.athlete_view.domain.health.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate

@Entity
class Health(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private var user: User,

    private var date: LocalDate,
    private var avgSteps: Int,
    private var avgBPM: Int,
    private var avgSleepDuration: Int, // in minutes
)
