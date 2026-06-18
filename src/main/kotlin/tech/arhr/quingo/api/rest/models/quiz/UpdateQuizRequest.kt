package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.Visibility

data class UpdateQuizRequest(
    val visibility: Visibility,
)
