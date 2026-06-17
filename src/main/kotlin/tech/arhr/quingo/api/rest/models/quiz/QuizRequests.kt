package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateQuizRequest(
    @field:NotBlank
    val title: String,
    val description: String? = null,
    val visibility: Visibility,
)

data class UpdateQuizRequest(
    val visibility: Visibility,
)

/** Вариант ответа в запросе сохранения черновика. */
data class CardOptionInput(
    val text: String,
    val isCorrect: Boolean,
)

/**
 * Карточка в запросе сохранения черновика.
 */
data class CardInput(
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<CardOptionInput>? = null,
    val acceptedTexts: List<String>? = null,
)


data class SaveDraftRequest(
    @field:NotBlank
    val title: String,
    val description: String? = null,
    @field:Size(max = 60)
    @field:Valid
    val cards: List<CardInput>,
)
