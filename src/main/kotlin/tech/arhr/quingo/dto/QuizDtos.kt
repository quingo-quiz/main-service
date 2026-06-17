package tech.arhr.quingo.dto

import java.time.Instant
import java.util.UUID

data class CardOptionDto(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
)

data class CardDto(
    val id: UUID,
    val position: Int,
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<CardOptionDto>? = null,
    val acceptedTexts: List<String>? = null,
)

data class QuizContentDto(
    val id: UUID,
    val title: String,
    val description: String? = null,
    val cards: List<CardDto>,
    val modifiedAt: Instant? = null,
    val createdAt: Instant? = null,
)

data class QuizDto(
    val id: UUID,
    val ownerId: UUID,
    val visibility: Visibility,
    val status: QuizStatus,
    val draft: QuizContentDto? = null,
    val snapshot: QuizContentDto? = null,
)

data class QuizSummaryDto(
    val id: UUID,
    val version: QuizVersion,
    val title: String,
    val description: String? = null,
    val status: QuizStatus,
    val visibility: Visibility,
    val cardCount: Int,
    val modifiedAt: Instant,
    val createdAt: Instant,
)
