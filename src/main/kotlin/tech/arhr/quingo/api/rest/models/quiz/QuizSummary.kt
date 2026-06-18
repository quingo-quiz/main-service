package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.QuizStatus
import tech.arhr.quingo.dto.QuizVersion
import tech.arhr.quingo.dto.Visibility
import java.time.Instant
import java.util.UUID

data class QuizSummary(
    val id: UUID,
    val version: QuizVersion,
    val title: String,
    val description: String? = null,
    val status: QuizStatus,
    val visibility: Visibility,
    val cardCount: Int,
    val modifiedAt: Instant,
    val createdAt: Instant,
)
