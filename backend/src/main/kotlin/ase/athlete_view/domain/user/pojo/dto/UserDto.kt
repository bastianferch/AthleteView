package ase.athlete_view.domain.user.pojo.dto

import lombok.*

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
class UserDto(
    val id: Long? = null,
    val name: String? = null
)
