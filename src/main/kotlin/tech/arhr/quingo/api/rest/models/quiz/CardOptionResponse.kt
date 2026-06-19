package tech.arhr.quingo.api.rest.models.quiz

import com.fasterxml.jackson.annotation.JsonProperty

data class CardOptionResponse(
    val id: Int,
    val text: String,
    // Имя поля в ответе — `isCorrect` (иначе Kotlin-свойство сериализуется как `correct`).
    @get:JsonProperty("isCorrect")
    val isCorrect: Boolean,
)
