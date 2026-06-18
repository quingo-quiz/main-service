package tech.arhr.quingo.service

import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.dto.QuizDto
import tech.arhr.quingo.dto.QuizSummaryDto
import tech.arhr.quingo.dto.Visibility
import java.util.UUID

interface QuizService {

    fun listSummaries(ownerId: UUID): List<QuizSummaryDto>

    fun createQuiz(ownerId: UUID, request: CreateQuizRequest): QuizDto

    fun get(ownerId: UUID, quizId: UUID): QuizDto

    fun update(ownerId: UUID, quizId: UUID, visibility: Visibility): QuizDto

    fun delete(ownerId: UUID, quizId: UUID)

    fun createDraft(ownerId: UUID, quizId: UUID): QuizDto

    fun saveDraft(ownerId: UUID, quizId: UUID, request: SaveDraftRequest): QuizDto

    fun publish(ownerId: UUID, quizId: UUID): QuizDto

    fun deleteDraft(ownerId: UUID, quizId: UUID)
}
