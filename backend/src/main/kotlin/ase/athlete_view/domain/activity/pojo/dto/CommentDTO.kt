package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Comment
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import java.time.LocalDateTime

open class CommentDTO(
    var id: Long?,
    var text: String,
    var author: UserDTO? = null, //only used to get information from backend to frontend. Backend never relies on this when received from frontend!
    var date: LocalDateTime? = null,
    ) {
    open fun toEntity(): Comment {
        return Comment(
            id = id,
            text = text,
            author = null,
            date = null,
        )
    }
}