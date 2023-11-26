package ase.athlete_view.domain.mail.service

import ase.athlete_view.domain.mail.pojo.entity.Email

interface MailService {
    /**
     * Sends the provided email.
     *
     * @param email must have the sender.
     */
    fun sendSimpleMail(email: Email)
}