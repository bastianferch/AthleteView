package ase.athlete_view.domain.csp.pojo.entity

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.entity.Trainer
import jakarta.persistence.*

@Entity
@Table(name = "csp_job",
        uniqueConstraints = [
            UniqueConstraint(
                    columnNames = ["trainer_id","date"],
                    name = "uc_trainer_date"
            )
        ])
open class CspJob(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        open var id: Long?,

        @OneToMany
        open var activities: MutableList<PlannedActivity>,

        @ManyToOne
        @JoinColumn(name = "trainer_id")
        open var trainer: Trainer,

        open var date: String
)
