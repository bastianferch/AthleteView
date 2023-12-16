package ase.athlete_view.domain.notification.persistence

import ase.athlete_view.domain.notification.pojo.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
}