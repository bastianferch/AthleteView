package ase.athlete_view.domain.time_constraint.pojo.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.DayOfWeek
import java.time.LocalTime

@Embeddable
class TimeFrame (

        var weekday: DayOfWeek,
        @Column(columnDefinition = "TIME")
        var startTime: LocalTime,
        @Column(columnDefinition = "TIME")
        var endTime: LocalTime
) {
        override fun toString(): String {
                return "TimeFrame(weekday=$weekday, startTime=$startTime, endTime=$endTime)"
        }
}