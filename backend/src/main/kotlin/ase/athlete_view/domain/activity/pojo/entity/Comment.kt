package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ActivityComment")
open class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,

    @Column(columnDefinition="TEXT")
    open var text: String,

    @ManyToOne
    open var author: User?,

    open var date: LocalDateTime?,
) {

    open fun toDTO(): CommentDTO {
        return CommentDTO(
            id = id,
            text = text,
            author = author?.toUserDTO(),
            date = date,
        )
    }

}