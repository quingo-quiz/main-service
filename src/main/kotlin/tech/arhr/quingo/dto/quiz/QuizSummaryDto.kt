package tech.arhr.quingo.dto.quiz

import java.time.Instant
import java.util.UUID

data class QuizSummaryDto(
    val id: UUID,
    val title: String? = null,
    val description: String? = null,
    val status: QuizStatus,
    val visibility: Visibility,
    val cardCount: Int,
    val modifiedAt: Instant,
    val createdAt: Instant,
)
