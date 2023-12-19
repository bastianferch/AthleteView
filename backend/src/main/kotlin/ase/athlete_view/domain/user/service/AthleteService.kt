package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.entity.Athlete

interface AthleteService {

    /**
     * Gets all users for a given trainer id
     * @throws NotFoundException if user was not found.
     */
    fun getByTrainerId(id: Long): List<Athlete>


    fun getById(id:Long): Athlete
}
