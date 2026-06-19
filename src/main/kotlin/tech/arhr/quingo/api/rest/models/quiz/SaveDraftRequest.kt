package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Тело сохранения черновика. Валидация мягкая: проверяются только ограничения
 * по максимальной длине и количеству. Полнота карточек (тип, непустой текст,
 * согласованность ответа с типом) проверяется при публикации, а не здесь.
 */
data class SaveDraftRequest(
    @field:Size(max = QuizConstraints.TITLE_MAX)
    val title: String? = null,
    @field:Size(max = QuizConstraints.DESCRIPTION_MAX)
    val description: String? = null,
    @field:NotNull
    @field:Size(max = QuizConstraints.CARDS_MAX)
    @field:Valid
    val cards: List<CardInput>?,
)
