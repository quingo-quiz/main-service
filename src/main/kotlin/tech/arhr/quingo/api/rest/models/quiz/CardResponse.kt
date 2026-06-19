package tech.arhr.quingo.api.rest.models.quiz

import tech.arhr.quingo.dto.CardType
import java.util.UUID

data class CardResponse(
    val id: UUID,
    val position: Int,
    val type: CardType? = null,
    val questionText: String? = null,
    val timerSeconds: Int? = null,
    val options: List<CardOptionResponse>? = null,
    val acceptedTexts: List<String>? = null,
)
