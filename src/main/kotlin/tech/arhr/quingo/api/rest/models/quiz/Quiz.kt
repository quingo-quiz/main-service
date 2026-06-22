package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.quiz.QuizStatus
import tech.arhr.quingo.dto.quiz.Visibility
import java.util.UUID

data class Quiz(
    val id: UUID,
    val ownerId: UUID,
    val visibility: Visibility,
    val status: QuizStatus,
    val draft: QuizContent? = null,
    val snapshot: QuizContent? = null,
)
