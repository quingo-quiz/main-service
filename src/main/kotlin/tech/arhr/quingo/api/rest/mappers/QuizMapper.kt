package tech.arhr.quingo.api.rest.mappers

import io.mcarle.konvert.api.Konverter
import tech.arhr.quingo.api.rest.models.quiz.Card
import tech.arhr.quingo.api.rest.models.quiz.CardInput
import tech.arhr.quingo.api.rest.models.quiz.CardOption
import tech.arhr.quingo.api.rest.models.quiz.CardOptionInput
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.Quiz
import tech.arhr.quingo.api.rest.models.quiz.QuizContent
import tech.arhr.quingo.api.rest.models.quiz.QuizSummary
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.api.rest.models.quiz.Visibility as ApiVisibility
import tech.arhr.quingo.dto.CardDraftDto
import tech.arhr.quingo.dto.CardDto
import tech.arhr.quingo.dto.CardOptionDto
import tech.arhr.quingo.dto.CreateQuizDto
import tech.arhr.quingo.dto.DraftContentDto
import tech.arhr.quingo.dto.OptionDraftDto
import tech.arhr.quingo.dto.QuizContentDto
import tech.arhr.quingo.dto.QuizDto
import tech.arhr.quingo.dto.QuizSummaryDto
import tech.arhr.quingo.dto.Visibility as DomainVisibility

@Konverter
interface QuizMapper {

    fun toApi(dto: QuizDto): Quiz
    fun toApi(dto: QuizContentDto): QuizContent
    fun toApi(dto: CardDto): Card
    fun toApi(dto: CardOptionDto): CardOption
    fun toApi(dto: QuizSummaryDto): QuizSummary

    fun toDto(request: CreateQuizRequest): CreateQuizDto
    fun toDto(request: SaveDraftRequest): DraftContentDto
    fun toDto(input: CardInput): CardDraftDto
    fun toDto(input: CardOptionInput): OptionDraftDto

    fun toDomain(visibility: ApiVisibility): DomainVisibility
}
