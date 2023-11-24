package ase.athlete_view.util

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition

@SpringBootTest
@ActiveProfiles("test")
class TestBase {
    @Autowired
    private lateinit var ur: UserRepository
    val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var txm: PlatformTransactionManager
    private lateinit var txStatus: TransactionStatus

    @BeforeEach
    fun setupDb() {
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        txStatus = txm.getTransaction(def)
        assumeTrue(txStatus.isNewTransaction)
        txStatus.setRollbackOnly()

        logger.info { "Configuring test user" }
        val u = User(null, "a@s.com", "Josef", "asdf", "Austria", "1337")
        ur.save(u)
    }

    @AfterEach
    fun teardownDb() {
        txm.rollback(txStatus)
    }
}