package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity
import java.time.DateTimeException
import java.time.LocalDateTime

class ActivityCreator {
        companion object {
            fun getDefaultActivity(): Activity {
                return Activity(
                    1L,
                    UserCreator.getUser(),
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
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(120)
                )
            }
        }
}