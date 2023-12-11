package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.entity.Athlete

interface AthleteService {

    fun getById(id:Long): Athlete
}
