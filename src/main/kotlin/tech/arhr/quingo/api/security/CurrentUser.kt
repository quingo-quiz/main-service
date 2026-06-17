package tech.arhr.quingo.api.security

import jakarta.enterprise.context.RequestScoped
import java.util.UUID

/** Идентичность пользователя из заголовков gateway, доступная на время запроса. */
@RequestScoped
class CurrentUser {
    lateinit var id: UUID
    var email: String? = null
    var roles: Set<String> = emptySet()
    lateinit var sessionId: UUID
}
