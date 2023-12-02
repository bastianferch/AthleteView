package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity

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
                    ""
                )
            }
        }
}