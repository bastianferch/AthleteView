package ase.athlete_view.domain.authentication.dto

import ase.athlete_view.domain.user.pojo.entity.Athlete
import java.time.LocalDate

data class AthleteRegistrationDTO
    (
    override var email: String?,
    override var name: String?,
    override var password: String?,
    override var country: String?,
    override var zip: String?,
    var dob: LocalDate?,
    var height: Int?,
    var weight: Int?,
    var code: String?
) :
    RegistrationDTO(email, name, password, country, zip) {

        fun toEntity(): Athlete {
            return Athlete(
                null,
                this.email!!,
                this.name!!,
                this.password!!,
                this.country,
                this.zip,
                this.dob!!,
                this.height!!,
                this.weight!!,
                null,
                null


            )
        }
    override fun toString(): String {
        return "AthleteRegistrationDTO(email=$email, name=$name, country=$country, zip=$zip, dob=$dob, height=$height, weight=$weight, code=$code)"
    }
}
