package tech.arhr.quingo.api.rest.mappers

import io.mcarle.konvert.api.Konverter
import tech.arhr.quingo.api.rest.models.quiz.CardOptionResponse
import tech.arhr.quingo.api.rest.models.quiz.CardResponse
import tech.arhr.quingo.api.rest.models.quiz.Quiz
import tech.arhr.quingo.api.rest.models.quiz.QuizContent
import tech.arhr.quingo.api.rest.models.quiz.QuizSummary
import tech.arhr.quingo.dto.CardDto
import tech.arhr.quingo.dto.CardOptionDto
import tech.arhr.quingo.dto.QuizContentDto
import tech.arhr.quingo.dto.QuizDto
import tech.arhr.quingo.dto.QuizSummaryDto

@Konverter
interface QuizMapper {
    fun toApi(dto: QuizDto): Quiz
    fun toApi(dto: QuizContentDto): QuizContent
    fun toApi(dto: CardDto): CardResponse
    fun toApi(dto: CardOptionDto): CardOptionResponse
    fun toApi(dto: QuizSummaryDto): QuizSummary
}
