package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

class PermissionDeniedException : QuingoAppException(
    "Access denied",
    Response.Status.FORBIDDEN,
)
