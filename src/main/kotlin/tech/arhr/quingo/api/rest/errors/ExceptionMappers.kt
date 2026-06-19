package tech.arhr.quingo.api.rest.errors

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import io.quarkus.logging.Log
import jakarta.annotation.Priority
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ElementKind
import jakarta.validation.Path
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

    override fun toResponse(exception: QuingoAppException): Response =
        errorResponse(exception.status, exception.message, uriInfo, request, exception.fieldErrors)
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
            val field = renderPath(violation.propertyPath)
            fieldErrors[field] = violation.message
            rejectedValues[field] = violation.invalidValue
        }
        return errorResponse(Response.Status.BAD_REQUEST, "Validation failed", uriInfo, request, fieldErrors, rejectedValues)
    }
}

/**
 * Перехватывает ошибки десериализации тела (неверный enum, неверный тип,
 * неизвестное или отсутствующее поле) и приводит их к единому формату
 * fieldErrors + rejectedValues. @Priority(1) перебивает встроенный маппер Quarkus.
 */
@Priority(1)
@Provider
class MismatchedInputExceptionMapper : ExceptionMapper<MismatchedInputException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: MismatchedInputException): Response {
        val field = when (exception) {
            is UnrecognizedPropertyException -> exception.propertyName ?: buildJsonPath(exception.path)
            else -> buildJsonPath(exception.path)
        }.ifBlank { "body" }
        val rejected: Any? = (exception as? InvalidFormatException)?.value
        return errorResponse(
            Response.Status.BAD_REQUEST, "Validation failed", uriInfo, request,
            mapOf(field to describe(exception)), mapOf(field to rejected),
        )
    }

    private fun describe(exception: MismatchedInputException): String = when (exception) {
        is UnrecognizedPropertyException -> "unknown field"
        is InvalidFormatException -> {
            val type = exception.targetType
            if (type != null && type.isEnum) {
                "must be one of: " + type.enumConstants.joinToString(", ") { (it as Enum<*>).name }
            } else {
                "invalid value for type ${type?.simpleName ?: "unknown"}"
            }
        }
        else -> "missing or invalid value"
    }
}

@Provider
class JsonProcessingExceptionMapper : ExceptionMapper<JsonProcessingException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: JsonProcessingException): Response =
        errorResponse(Response.Status.BAD_REQUEST, "Request body is missing or malformed", uriInfo, request)
}

@Provider
class WebApplicationExceptionMapper : ExceptionMapper<WebApplicationException> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: WebApplicationException): Response =
        errorResponse(exception.response.statusInfo, exception.message, uriInfo, request)
}

@Provider
class UncaughtExceptionMapper : ExceptionMapper<Throwable> {
    @Context lateinit var uriInfo: UriInfo
    @Context lateinit var request: Request

    override fun toResponse(exception: Throwable): Response {
        Log.error("Unhandled exception", exception)
        return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", uriInfo, request)
    }
}

/**
 * Рендерит путь Bean Validation в нотацию `cards[0].timerSeconds`, отбрасывая
 * служебные узлы метода/параметра (напр. `saveDraft.request`).
 */
internal fun renderPath(path: Path): String {
    val sb = StringBuilder()
    for (node in path) {
        if (node.kind != ElementKind.PROPERTY) continue
        node.index?.let { sb.append('[').append(it).append(']') }
            ?: node.key?.let { sb.append('[').append(it).append(']') }
        if (node.name != null) {
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(node.name)
        }
    }
    return sb.toString().ifBlank { path.lastOrNull()?.name ?: "request" }
}

/** Собирает путь до поля из JSON-цепочки Jackson, напр. `cards[0].timerSeconds`. */
internal fun buildJsonPath(refs: List<JsonMappingException.Reference>): String {
    val sb = StringBuilder()
    for (ref in refs) {
        when {
            ref.fieldName != null -> {
                if (sb.isNotEmpty()) sb.append('.')
                sb.append(ref.fieldName)
            }
            ref.index >= 0 -> sb.append('[').append(ref.index).append(']')
        }
    }
    return sb.toString()
}
