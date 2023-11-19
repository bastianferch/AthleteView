package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.util.ActivityType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.util.*

@Entity
class PlannedActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    val type: ActivityType,

    @OneToOne
    val interval: Interval,

    val withTrainer: Boolean,

    val template: Boolean,

    val note: String?,

    val date: Date?
)