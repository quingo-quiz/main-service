package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SaveDraftRequest(
    @field:NotBlank
    @field:Size(max = QuizConstraints.TITLE_MAX)
    val title: String,
    @field:Size(max = QuizConstraints.DESCRIPTION_MAX)
    val description: String? = null,
    @field:Size(max = QuizConstraints.CARDS_MAX)
    @field:Valid
    val cards: List<CardInput>,
)
