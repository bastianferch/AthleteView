import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.util.WithCustomMockUser
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory


class CustomWithMockCustomUserSecurityContextFactory :
    WithSecurityContextFactory<WithCustomMockUser> {

    override fun createSecurityContext(customUser: WithCustomMockUser): SecurityContext {
        val userDto = UserDto(
            id = customUser.id,
            name = customUser.name,
            email = customUser.email,
            password = customUser.password,
            token = customUser.token
        )
        val context = SecurityContextHolder.createEmptyContext()
        val principal = userDto
        val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_USER")) // TODO change if Roles are implemented
        val auth: Authentication = UsernamePasswordAuthenticationToken(principal, userDto.password,authorities)
        context.authentication= auth


        return context
    }
}
