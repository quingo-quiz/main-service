package tech.arhr.quingo.api.rest.models

import java.time.Instant

/**
 * Универсальный конверт ошибки. Заполняется обработчиками исключений.
 *
 * - [fieldErrors] — сообщения валидации.
 * - [rejectedValues] — отклонённые значения по полям.
 * - [details] — произвольный контекст ошибки.
 */
data class ErrorResponse(
    val status: Int,
    val statusMessage: String,
    val message: String?,
    val path: String?,
    val method: String?,
    val timestamp: Instant,
    val fieldErrors: Map<String, String>? = null,
    val rejectedValues: Map<String, Any?>? = null,
    val details: Map<String, Any?>? = null,
)
