package ase.athlete_view.util

import ase.athlete_view.domain.user.persistence.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition

@SpringBootTest
@ActiveProfiles("test")
class TestBase {
    @Autowired
    private lateinit var txm: PlatformTransactionManager
    private lateinit var txStatus: TransactionStatus

    @Autowired
    private lateinit var ur: UserRepository

    private val logger = KotlinLogging.logger {}

    @BeforeEach
    fun setupDb() {
        logger.info { "Configuring transaction for testcase" }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        txStatus = txm.getTransaction(def)
        assumeTrue(txStatus.isNewTransaction)
        txStatus.setRollbackOnly()
    }

    @AfterEach
    fun teardownDb() {
        txm.rollback(txStatus)
    }

    protected fun createDefaultUserInDb() {
        ur.save(UserCreator.getUser())
    }
}