package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

class EntityNotFoundException(message: String) :
    QuingoAppException(message, Response.Status.NOT_FOUND)
