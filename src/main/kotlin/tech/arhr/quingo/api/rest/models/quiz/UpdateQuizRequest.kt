package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.constraints.NotNull
import tech.arhr.quingo.dto.Visibility

data class UpdateQuizRequest(
    @field:NotNull
    val visibility: Visibility?,
)
