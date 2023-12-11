package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.entity.Athlete
import org.springframework.stereotype.Service

@Service
interface AthleteService {

    fun getById(id:Long): Athlete
}
