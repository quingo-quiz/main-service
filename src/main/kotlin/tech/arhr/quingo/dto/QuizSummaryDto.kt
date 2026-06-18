package tech.arhr.quingo.dto

import java.time.Instant
import java.util.UUID

data class QuizSummaryDto(
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
