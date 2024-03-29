/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.util

import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.Trainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition

const val TRAINER_ID = -3L
const val ATHLETE_ID = -2L

@SpringBootTest
@DirtiesContext
class TestBase {
    @Autowired
    private lateinit var txm: PlatformTransactionManager
    private lateinit var txStatus: TransactionStatus

    @Autowired
    private lateinit var ur: UserRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var nr: NotificationRepository

    private val log = KotlinLogging.logger {}
    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setupDb() {
        log.info { "Configuring transaction for testcase" }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        txStatus = txm.getTransaction(def)
        Assumptions.assumeTrue(txStatus.isNewTransaction)
        txStatus.setRollbackOnly()
    }

    @AfterEach
    fun teardownDb() {
        txm.rollback(txStatus)
    }

    protected fun createDefaultUserInDb(email: String = UserCreator.DEFAULT_ATHLETE_EMAIL): Athlete {
        val user = UserCreator.getAthlete(null)
        user.password = encoder.encode(user.password)
        user.isConfirmed = true
        user.trainer = null
        user.email = email
        user.preferences = Preferences(null)
        return ur.save(user)
    }

    protected fun createDefaultUnconfirmedAthlete(): Athlete{
        val user = UserCreator.getAthlete(null)
        user.password = encoder.encode(user.password)
        user.isConfirmed = false
        user.trainer = null
        user.email = UserCreator.DEFAULT_NEW_ATHLETE_EMAIL
        return ur.save(user)
    }

    protected fun persistDefaultTrainer(id: Long): Trainer {
        val trainer = UserCreator.getTrainer()
        trainer.id = id
        return ur.save(trainer)
    }

    protected fun addAthleteToTrainer(athlete: Athlete, trainer: Trainer): Trainer {
        val trainerOption = ur.findById(trainer.id!!).get() as Trainer
        val user = ur.findById(athlete.id!!)
        trainerOption.athletes.add(user.get() as Athlete)
        return ur.save(trainerOption)
    }

    protected fun createDefaultTrainerAthleteRelationInDb(): Pair<Athlete, Trainer> {
        val athlete = UserCreator.getAthlete(null)
        athlete.password = encoder.encode(athlete.password)
        athlete.isConfirmed = true
        athlete.trainer = null

        val trainer = UserCreator.getTrainer()
        trainer.password = encoder.encode(trainer.password)
        trainer.isConfirmed = true
        trainer.athletes = mutableSetOf(athlete)

        var ares = ur.save(athlete)
        val tres = ur.save(trainer)

        ares.trainer = tres
        ares = ur.save(ares)

        return ares to tres
    }
}
