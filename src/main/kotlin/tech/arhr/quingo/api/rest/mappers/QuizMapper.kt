package tech.arhr.quingo.api.rest.mappers

import io.mcarle.konvert.api.Konverter
import tech.arhr.quingo.api.rest.models.quiz.CardOptionResponse
import tech.arhr.quingo.api.rest.models.quiz.CardResponse
import tech.arhr.quingo.api.rest.models.quiz.Quiz
import tech.arhr.quingo.api.rest.models.quiz.QuizContent
import tech.arhr.quingo.api.rest.models.quiz.QuizSummary
import tech.arhr.quingo.dto.quiz.CardDto
import tech.arhr.quingo.dto.quiz.CardOptionDto
import tech.arhr.quingo.dto.quiz.QuizContentDto
import tech.arhr.quingo.dto.quiz.QuizDto
import tech.arhr.quingo.dto.quiz.QuizSummaryDto

@Konverter
interface QuizMapper {
    fun toApi(dto: QuizDto): Quiz
    fun toApi(dto: QuizContentDto): QuizContent
    fun toApi(dto: CardDto): CardResponse
    fun toApi(dto: CardOptionDto): CardOptionResponse
    fun toApi(dto: QuizSummaryDto): QuizSummary
}
