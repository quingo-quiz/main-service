package tech.arhr.quingo.exceptions

import jakarta.ws.rs.core.Response

private val UNPROCESSABLE_ENTITY: Response.StatusType = object : Response.StatusType {
    override fun getStatusCode() = 422
    override fun getReasonPhrase() = "Unprocessable Entity"
    override fun getFamily() = Response.Status.Family.CLIENT_ERROR
}

class UnprocessableEntityException(message: String, fieldErrors: Map<String, String>) :
    QuingoAppException(message, UNPROCESSABLE_ENTITY, fieldErrors)
