package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

class PermissionDeniedException(message: String = "Permission denied for this action") :
    QuingoAppException(message, Response.Status.FORBIDDEN)
