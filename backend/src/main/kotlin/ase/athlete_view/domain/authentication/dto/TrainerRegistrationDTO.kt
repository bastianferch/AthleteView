package ase.athlete_view.domain.authentication.dto

data class TrainerRegistrationDTO
    (
    override var email: String?,
    override var name: String?,
    override var password: String?,
    override var country: String?,
    override var zip: String?
) :
    RegistrationDTO(email, name, password, country, zip){
}