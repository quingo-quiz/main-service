package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.constraints.Size

data class CardOptionInput(
    @field:Size(max = QuizConstraints.OPTION_TEXT_MAX)
    val text: String,
    val isCorrect: Boolean,
)
