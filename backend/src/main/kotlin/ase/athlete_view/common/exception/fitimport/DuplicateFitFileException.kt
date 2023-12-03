package ase.athlete_view.common.exception.fitimport

import lombok.NoArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@NoArgsConstructor
@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateFitFileException(message: String?) : RuntimeException(message)