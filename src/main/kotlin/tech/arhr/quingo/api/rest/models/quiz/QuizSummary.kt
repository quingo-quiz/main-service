package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.quiz.QuizStatus
import tech.arhr.quingo.dto.quiz.Visibility
import java.time.Instant
import java.util.UUID

data class QuizSummary(
    val id: UUID,
    val title: String? = null,
    val description: String? = null,
    val status: QuizStatus,
    val visibility: Visibility,
    val cardCount: Int,
    val modifiedAt: Instant,
    val createdAt: Instant,
)
