package tech.arhr.quingo.service

import tech.arhr.quingo.dto.CreateQuizDto
import tech.arhr.quingo.dto.DraftContentDto
import tech.arhr.quingo.dto.QuizDto
import tech.arhr.quingo.dto.QuizSummaryDto
import tech.arhr.quingo.dto.Visibility
import java.util.UUID

interface QuizService {

    fun listSummaries(ownerId: UUID): List<QuizSummaryDto>

    fun create(ownerId: UUID, command: CreateQuizDto): QuizDto

    fun get(ownerId: UUID, quizId: UUID): QuizDto

    fun changeVisibility(ownerId: UUID, quizId: UUID, visibility: Visibility): QuizDto

    fun delete(ownerId: UUID, quizId: UUID)

    fun startEditing(ownerId: UUID, quizId: UUID): QuizDto

    fun saveDraft(ownerId: UUID, quizId: UUID, content: DraftContentDto): QuizDto

    fun publish(ownerId: UUID, quizId: UUID): QuizDto

    fun discardDraft(ownerId: UUID, quizId: UUID)
}
