package tech.arhr.quingo.dto

import java.util.UUID

data class CardDto(
    val id: UUID,
    val position: Int,
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<CardOptionDto>? = null,
    val acceptedTexts: List<String>? = null,
)
