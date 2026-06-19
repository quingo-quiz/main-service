package tech.arhr.quingo.api.rest.models.quiz

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Size

/**
 * Вариант ответа в черновике. Поля необязательны (мягкая валидация черновика);
 * непустой текст и заданный признак правильности проверяются при публикации.
 */
data class CardOptionInput(
    @field:Size(max = QuizConstraints.OPTION_TEXT_MAX)
    val text: String? = null,
    // Без явного имени Jackson мапит Kotlin-свойство `isCorrect` на JSON-поле `correct`
    // (срезает префикс `is`), из-за чего входящее `isCorrect` терялось.
    @param:JsonProperty("isCorrect")
    @get:JsonProperty("isCorrect")
    val isCorrect: Boolean? = null,
)
