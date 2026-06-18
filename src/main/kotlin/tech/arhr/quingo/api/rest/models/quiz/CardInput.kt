package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import tech.arhr.quingo.dto.CardType

data class CardInput(
    val type: CardType,
    @field:Size(max = QuizConstraints.QUESTION_TEXT_MAX)
    val questionText: String,
    val timerSeconds: Int,
    @field:Size(max = QuizConstraints.OPTIONS_MAX)
    @field:Valid
    val options: List<CardOptionInput>? = null,
    @field:Size(max = QuizConstraints.ACCEPTED_TEXTS_MAX)
    val acceptedTexts: List<@Size(max = QuizConstraints.ACCEPTED_TEXT_MAX) String>? = null,
)
