package tech.arhr.quingo.dto

import java.util.UUID

data class QuizDto(
    val id: UUID,
    val ownerId: UUID,
    val visibility: Visibility,
    val status: QuizStatus,
    val draft: QuizContentDto? = null,
    val snapshot: QuizContentDto? = null,
)
