package tech.arhr.quingo.api.rest.models.quiz

data class CardOptionResponse(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
)
