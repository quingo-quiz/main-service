package tech.arhr.quingo.dto.quiz

import java.time.Instant
import java.util.UUID

data class QuizContentDto(
    val id: UUID,
    val title: String? = null,
    val description: String? = null,
    val cards: List<CardDto>,
    val modifiedAt: Instant? = null,
    val createdAt: Instant? = null,
)
