package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

class ConflictException(message: String) :
    QuingoAppException(message, Response.Status.CONFLICT)
