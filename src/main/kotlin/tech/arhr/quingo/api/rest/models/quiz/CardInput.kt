package tech.arhr.quingo.api.rest.models.quiz

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import tech.arhr.quingo.dto.CardType

/**
 * Карточка в теле черновика. Все содержательные поля необязательны — карточка
 * может находиться в любом промежуточном состоянии. Ограничены только максимальные
 * длины и количество. Строгие проверки (тип задан, текст непуст, ответ согласован
 * с типом) выполняются при публикации.
 */
data class CardInput(
    val type: CardType? = null,
    @field:Size(max = QuizConstraints.QUESTION_TEXT_MAX)
    val questionText: String? = null,
    val timerSeconds: Int? = null,
    @field:Size(max = QuizConstraints.OPTIONS_MAX)
    @field:Valid
    val options: List<CardOptionInput>? = null,
    @field:Size(max = QuizConstraints.ACCEPTED_TEXTS_MAX)
    val acceptedTexts: List<@Size(max = QuizConstraints.ACCEPTED_TEXT_MAX) String>? = null,
)
