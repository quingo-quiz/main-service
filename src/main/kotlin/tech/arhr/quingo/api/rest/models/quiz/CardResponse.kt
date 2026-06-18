package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.CardType
import java.util.UUID

data class CardResponse(
    val id: UUID,
    val position: Int,
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<CardOptionResponse>? = null,
    val acceptedTexts: List<String>? = null,
)
