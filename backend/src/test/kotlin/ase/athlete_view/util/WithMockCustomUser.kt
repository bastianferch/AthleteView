package ase.athlete_view.util

import org.springframework.security.test.context.support.WithSecurityContext


@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = CustomWithMockCustomUserSecurityContextFactory::class)
annotation class WithCustomMockUser(
    val id: Long = 1L,
    val name: String = "testUser",
    val email: String = "test@example.com",
    val password: String = "password",
    val token: String = "token"
)