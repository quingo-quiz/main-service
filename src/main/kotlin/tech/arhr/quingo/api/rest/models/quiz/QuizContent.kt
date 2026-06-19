package tech.arhr.quingo.api.rest.models.quiz

import java.time.Instant
import java.util.UUID

data class QuizContent(
    val id: UUID,
    val title: String? = null,
    val description: String? = null,
    val cards: List<CardResponse>,
    val modifiedAt: Instant? = null,
    val createdAt: Instant? = null,
)
