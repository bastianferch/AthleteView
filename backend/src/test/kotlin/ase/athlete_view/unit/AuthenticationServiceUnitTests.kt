package ase.athlete_view.unit

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ActiveProfiles("test")
class AuthenticationServiceUnitTests {
    @MockkBean
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var userService: UserService

    private val user = User(1, "a@b.com", "Max Mustermann", "musterpassword", "sampletokendefinvalid", "1337")

    @Test
    fun testUserRepo() {
        every { userRepo.findByEmail("a@b.com") } returns user

        val usrData = userService.getByEmail("a@b.com")
        verify { userRepo.findByEmail("a@b.com") }
        assert(usrData == user)
    }
}