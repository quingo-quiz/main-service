package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

class EntityNotFoundException(entity: String) : QuingoAppException(
    "$entity not found",
    Response.Status.NOT_FOUND,
)
