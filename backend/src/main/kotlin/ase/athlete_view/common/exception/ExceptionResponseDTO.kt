package ase.athlete_view.common.exception

import lombok.AllArgsConstructor
import lombok.Builder
import org.springframework.http.HttpStatus

@AllArgsConstructor
@Builder
class ExceptionResponseDTO (
    var status: HttpStatus? = null,
    var message: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)
