package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import tech.arhr.quingo.dto.Visibility

data class CreateQuizRequest(
    @field:NotBlank
    @field:Size(max = QuizConstraints.TITLE_MAX)
    val title: String,
    @field:Size(max = QuizConstraints.DESCRIPTION_MAX)
    val description: String? = null,
    val visibility: Visibility,
)
