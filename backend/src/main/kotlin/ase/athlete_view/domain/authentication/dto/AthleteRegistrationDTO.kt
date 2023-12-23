package ase.athlete_view.domain.authentication.dto

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
    override fun toString(): String {
        return "AthleteRegistrationDTO(email=$email, name=$name, country=$country, zip=$zip, dob=$dob, height=$height, weight=$weight, code=$code)"
    }
}
