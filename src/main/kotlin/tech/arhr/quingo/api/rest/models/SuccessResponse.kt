package tech.arhr.quingo.api.rest.models

import jakarta.ws.rs.core.Response
import java.time.Instant

/**
 * Универсальный конверт успешного ответа.
 */
data class SuccessResponse<T>(
    val status: Int,
    val statusMessage: String,
    val message: String?,
    val data: T?,
    val timestamp: Instant,
) {
    companion object {
        fun <T> of(status: Response.Status, data: T?, timestamp: Instant): SuccessResponse<T> =
            SuccessResponse(status.statusCode, status.reasonPhrase, null, data, timestamp)

        fun <T> of(status: Response.Status, message: String?, data: T?, timestamp: Instant): SuccessResponse<T> =
            SuccessResponse(status.statusCode, status.reasonPhrase, message, data, timestamp)
    }
}
