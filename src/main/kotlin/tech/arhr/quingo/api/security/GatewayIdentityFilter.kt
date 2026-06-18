package tech.arhr.quingo.api.security

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider
import java.util.UUID

@Provider
class GatewayIdentityFilter(private val currentUser: CurrentUser) : ContainerRequestFilter {

    override fun filter(context: ContainerRequestContext) {
        context.getHeaderString(USER_ID)?.let { currentUser.id = UUID.fromString(it) }
        context.getHeaderString(SESSION_ID)?.let { currentUser.sessionId = UUID.fromString(it) }
        currentUser.email = context.getHeaderString(USER_EMAIL)
        currentUser.roles = context.getHeaderString(USER_ROLES)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
    }

    private companion object {
        const val USER_ID = "X-User-Id"
        const val USER_EMAIL = "X-User-Email"
        const val USER_ROLES = "X-User-Roles"
        const val SESSION_ID = "X-Session-Id"
    }
}
