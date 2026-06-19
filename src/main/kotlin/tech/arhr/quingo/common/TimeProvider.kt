package tech.arhr.quingo.common

import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

interface TimeProvider {
    fun now(): Instant
}

@ApplicationScoped
class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
}
