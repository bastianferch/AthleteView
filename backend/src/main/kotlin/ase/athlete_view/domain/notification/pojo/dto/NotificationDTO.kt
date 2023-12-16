package ase.athlete_view.domain.notification.pojo.dto

class NotificationDTO(
    var id: Long?,
    var header: String,
    var body: String? = null,
    var link: String? = null,
    var read: Boolean,
    var timestamp: Long,
)