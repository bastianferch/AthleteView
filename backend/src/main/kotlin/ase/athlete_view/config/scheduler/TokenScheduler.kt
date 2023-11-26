package ase.athlete_view.config.scheduler

import ase.athlete_view.domain.token.service.TokenService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.scheduling.enable"], havingValue = "true", matchIfMissing = true)
class TokenScheduler(private val tokenService: TokenService) {
    // every saturday at 06:00.
    @Scheduled(cron = "0 0 6 ? * SAT")
    fun deleteUnusedOrganizations() {
        this.tokenService.deleteExpiredTokens()
    }
}

