package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.util.TestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus


@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AuthenticationControllerIntegrationTests: TestBase() {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun loginWithCorrectCredentials_ShouldReturnOk() {
        val login = LoginDTO("a@s.com", "asdf")
        val result = restTemplate.postForEntity("/api/auth/login", login, UserDto::class.java)

        assert(result != null)
        assert(HttpStatus.OK == result?.statusCode)
        assert(result?.hasBody() == true)
        val dto = result?.body
        assert(dto?.email == "a@s.com")
        assert(dto?.password == "asdf")
        assert(dto?.token != null)
        assert(dto?.id != null)
    }
}