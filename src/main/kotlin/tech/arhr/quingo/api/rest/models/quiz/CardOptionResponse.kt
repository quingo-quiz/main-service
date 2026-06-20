package tech.arhr.quingo.api.rest.models.quiz

import com.fasterxml.jackson.annotation.JsonProperty

data class CardOptionResponse(
    val id: Int,
    val text: String,
    @get:JsonProperty("isCorrect")
    val isCorrect: Boolean,
)
