package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

open class QuingoAppException(
    message: String,
    val status: Response.StatusType = Response.Status.BAD_REQUEST,
    val fieldErrors: Map<String, String>? = null,
) : RuntimeException(message)
