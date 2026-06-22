package tech.arhr.quingo.dto.quiz

import java.util.UUID

data class CardDto(
    val id: UUID,
    val position: Int,
    val type: CardType? = null,
    val questionText: String? = null,
    val timerSeconds: Int? = null,
    val options: List<CardOptionDto>? = null,
    val acceptedTexts: List<String>? = null,
)
