package tech.arhr.quingo.api.rest.models.quiz

import java.time.Instant
import java.util.UUID

/** Вариант ответа карточки. */
data class CardOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
)

/** Карточка внутри квиза. */
data class Card(
    val id: UUID,
    val position: Int,
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<CardOption>? = null,
    val acceptedTexts: List<String>? = null,
)

/** Содержимое одного квиза. */
data class QuizContent(
    val id: UUID,
    val title: String,
    val description: String? = null,
    val cards: List<Card>,
    val modifiedAt: Instant? = null,
    val createdAt: Instant? = null,
)

/** Полноценная сущность квиза с идентичностью и контентом. */
data class Quiz(
    val id: UUID,
    val ownerId: UUID,
    val visibility: Visibility,
    val status: QuizStatus,
    val draft: QuizContent? = null,
    val snapshot: QuizContent? = null,
)

/**
 * Лёгкая сводка черновика или реального квиза.
 */
data class QuizSummary(
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
