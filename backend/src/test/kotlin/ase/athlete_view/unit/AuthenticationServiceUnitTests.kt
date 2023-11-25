package ase.athlete_view.unit

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.DefaultEntityCreatorUtil
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun testUserRepo() {
        val user = DefaultEntityCreatorUtil().getUser()
        every { userRepo.findByEmail("a@b.com") } returns user

        val usrData = userService.getByEmail("a@b.com")
        verify(exactly = 1) { userRepo.findByEmail("a@b.com") }
        assertThat(usrData).isEqualTo(user)
    }
}