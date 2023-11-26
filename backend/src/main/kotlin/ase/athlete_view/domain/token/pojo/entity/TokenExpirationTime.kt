package ase.athlete_view.domain.token.pojo.entity

import java.util.*

enum class TokenExpirationTime {
    FIVE_MIN,
    TEN_MIN,
    THIRTY_MIN,
    ONE_HOUR,
    ONE_DAY,
    ONE_YEAR;

    private var ms: Long = 0

    fun get(): Long {
        return ms
    }

    fun expirationDate(): Date {
        return Date(System.currentTimeMillis() + ms)
    }

    override fun toString(): String {
        return ms.toString()
    }
}