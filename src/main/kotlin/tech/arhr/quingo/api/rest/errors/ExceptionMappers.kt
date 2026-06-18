package tech.arhr.quingo.api.rest.errors

import io.quarkus.logging.Log
import jakarta.annotation.Priority
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import tech.arhr.quingo.exceptions.QuingoAppException

@Provider
class QuingoAppExceptionMapper : ExceptionMapper<QuingoAppException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: QuingoAppException): Response {
        val body = buildErrorResponse(exception.status, exception.message, uriInfo, request, exception.fieldErrors)
        return Response.status(exception.status.statusCode).entity(body).build()
    }
}

@Priority(1)
@Provider
class ConstraintViolationExceptionMapper : ExceptionMapper<ConstraintViolationException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: ConstraintViolationException): Response {
        val fieldErrors = LinkedHashMap<String, String>()
        val rejectedValues = LinkedHashMap<String, Any?>()
        exception.constraintViolations.forEach { violation ->
            val field = violation.propertyPath.lastOrNull()?.name ?: violation.propertyPath.toString()
            fieldErrors[field] = violation.message
            rejectedValues[field] = violation.invalidValue
        }
        val body = buildErrorResponse(
            Response.Status.BAD_REQUEST, "Validation failed", uriInfo, request, fieldErrors, rejectedValues,
        )
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build()
    }
}

@Provider
class WebApplicationExceptionMapper : ExceptionMapper<WebApplicationException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: WebApplicationException): Response {
        val status = exception.response.statusInfo
        val body = buildErrorResponse(status, exception.message, uriInfo, request)
        return Response.status(status.statusCode).entity(body).build()
    }
}

@Provider
class UncaughtExceptionMapper : ExceptionMapper<Throwable> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: Throwable): Response {
        Log.error("Unhandled exception", exception)
        val status = Response.Status.INTERNAL_SERVER_ERROR
        val body = buildErrorResponse(status, "Internal server error", uriInfo, request)
        return Response.status(status.statusCode).entity(body).build()
    }
}
