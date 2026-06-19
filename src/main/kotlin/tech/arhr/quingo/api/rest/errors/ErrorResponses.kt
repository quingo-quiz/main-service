package tech.arhr.quingo.api.rest.errors

import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import tech.arhr.quingo.api.rest.models.ErrorResponse
import java.time.Instant

internal fun errorResponse(
    status: Response.StatusType,
    message: String?,
    uriInfo: UriInfo,
    request: Request,
    fieldErrors: Map<String, String>? = null,
    rejectedValues: Map<String, Any?>? = null,
): Response {
    val body = ErrorResponse(
        status = status.statusCode,
        statusMessage = status.reasonPhrase,
        message = message,
        path = uriInfo.path,
        method = request.method,
        timestamp = Instant.now(),
        fieldErrors = fieldErrors,
        rejectedValues = rejectedValues,
    )
    return Response.status(status.statusCode).entity(body).build()
}
