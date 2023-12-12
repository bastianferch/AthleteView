package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity

class ActivityCreator {
        companion object {
            fun getDefaultActivity(): Activity {
                return Activity(
                    1L,
                    UserCreator.getAthlete(),
                    0,
                    0,
                    0,
                    0.0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    "",
                    null,
                    listOf()
                )
            }
        }
}