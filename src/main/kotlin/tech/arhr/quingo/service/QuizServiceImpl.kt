package tech.arhr.quingo.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tech.arhr.quingo.api.rest.models.quiz.CardInput
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.common.TimeProvider
import tech.arhr.quingo.dto.quiz.CardType
import tech.arhr.quingo.dto.quiz.QuizDto
import tech.arhr.quingo.dto.quiz.QuizStatus
import tech.arhr.quingo.dto.quiz.QuizSummaryDto
import tech.arhr.quingo.dto.quiz.Visibility
import tech.arhr.quingo.exceptions.EntityNotFoundException
import tech.arhr.quingo.exceptions.PermissionDeniedException
import tech.arhr.quingo.exceptions.QuingoAppException
import tech.arhr.quingo.persistence.entity.CardEntity
import tech.arhr.quingo.persistence.entity.OptionJson
import tech.arhr.quingo.persistence.entity.QuizDraftEntity
import tech.arhr.quingo.persistence.entity.QuizEntity
import tech.arhr.quingo.persistence.entity.QuizSnapshotEntity
import tech.arhr.quingo.persistence.mapper.QuizEntityMapperImpl
import tech.arhr.quingo.persistence.repository.QuizRepository
import java.util.UUID

@ApplicationScoped
@Transactional
class QuizServiceImpl(
    private val quizRepository: QuizRepository,
    private val time: TimeProvider,
) : QuizService {

    override fun listSummaries(ownerId: UUID): List<QuizSummaryDto> =
        quizRepository.findByOwner(ownerId).flatMap { it.toSummaries() }

    override fun createQuiz(ownerId: UUID, request: CreateQuizRequest): QuizDto {
        val now = time.now()

        val quiz = QuizEntity()
        quiz.ownerId = ownerId
        quiz.visibility = request.visibility!!

        val draft = QuizDraftEntity()
        draft.quiz = quiz
        draft.title = request.title
        draft.description = request.description
        draft.createdAt = now
        draft.modifiedAt = now
        quiz.draft = draft

        quizRepository.persist(quiz)
        return quiz.toDto()
    }

    override fun get(ownerId: UUID, quizId: UUID): QuizDto =
        findOrThrow(ownerId, quizId).toDto()

    override fun update(ownerId: UUID, quizId: UUID, visibility: Visibility): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        quiz.visibility = visibility
        return quiz.toDto()
    }

    override fun delete(ownerId: UUID, quizId: UUID) =
        quizRepository.delete(findOrThrow(ownerId, quizId))

    override fun createDraft(ownerId: UUID, quizId: UUID): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        if (quiz.draft != null) throw QuingoAppException("Quiz already has a draft")
        val snapshot = quiz.snapshot ?: throw QuingoAppException("Quiz has no published snapshot")

        val now = time.now()
        val draft = QuizDraftEntity()
        draft.quiz = quiz
        draft.title = snapshot.title
        draft.description = snapshot.description
        draft.createdAt = now
        draft.modifiedAt = now
        replaceCards(draft.cards, snapshot.cards) { newCard(draft) }
        quiz.draft = draft
        return quiz.toDto()
    }

    override fun saveDraft(ownerId: UUID, quizId: UUID, request: SaveDraftRequest): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        val draft = quiz.draft ?: throw QuingoAppException("Quiz has no draft")

        draft.title = request.title
        draft.description = request.description
        draft.modifiedAt = time.now()
        replaceCards(draft.cards, request.cards!!.map { it.toCard() }) { newCard(draft) }
        return quiz.toDto()
    }

    override fun publish(ownerId: UUID, quizId: UUID): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        val draft = quiz.draft ?: throw QuingoAppException("Quiz has no draft to publish")
        validateForPublish(draft)

        val now = time.now()
        val snapshot = quiz.snapshot ?: QuizSnapshotEntity().also {
            it.quiz = quiz
            it.createdAt = now
            quiz.snapshot = it
        }
        snapshot.title = draft.title!!
        snapshot.description = draft.description
        snapshot.modifiedAt = now
        replaceCards(snapshot.cards, draft.cards) { newCard(snapshot) }

        quiz.draft = null
        return quiz.toDto()
    }

    override fun deleteDraft(ownerId: UUID, quizId: UUID) {
        val quiz = findOrThrow(ownerId, quizId)
        if (quiz.draft == null) throw QuingoAppException("Quiz has no draft")
        if (quiz.snapshot == null) {
            quizRepository.delete(quiz)
        } else {
            quiz.draft = null
        }
    }

    // --- карточки ---

    /** Заменяет карточки из [target] на карточки из [sources] */
    private fun replaceCards(target: MutableList<CardEntity>, sources: List<CardEntity>, newCard: () -> CardEntity) {
        target.clear()
        sources.forEachIndexed { i, source ->
            target.add(newCard().also { source.copyContentTo(it, i) })
        }
    }

    private fun newCard(draft: QuizDraftEntity) = CardEntity().also { it.draft = draft }

    private fun newCard(snapshot: QuizSnapshotEntity) = CardEntity().also { it.snapshot = snapshot }

    private fun CardEntity.copyContentTo(target: CardEntity, position: Int) {
        target.position = position
        target.type = type
        target.questionText = questionText
        target.timerSeconds = timerSeconds
        target.options = options?.toMutableList()
        target.acceptedTexts = acceptedTexts?.toMutableList()
    }

    private fun CardInput.toCard() = CardEntity().also {
        it.type = type
        it.questionText = questionText
        it.timerSeconds = timerSeconds
        it.options = options?.takeIf { o -> o.isNotEmpty() }
            ?.mapIndexed { i, opt -> OptionJson(i, opt.text ?: "", opt.isCorrect ?: false) }
            ?.toMutableList()
        it.acceptedTexts = acceptedTexts?.takeIf { a -> a.isNotEmpty() }?.toMutableList()
    }

    // --- валидация публикации ---

    private fun validateForPublish(draft: QuizDraftEntity) {
        val errors = LinkedHashMap<String, String>()

        if (draft.title.isNullOrBlank()) errors["title"] = "must not be blank"
        if (draft.cards.isEmpty()) errors["cards"] = "must contain at least one card"
        draft.cards.forEachIndexed { i, card -> validateCardForPublish(card, "cards[$i]", errors) }

        if (errors.isNotEmpty()) {
            throw QuingoAppException("Cannot publish: quiz is incomplete", Response.Status.BAD_REQUEST, errors)
        }
    }

    private fun validateCardForPublish(card: CardEntity, path: String, errors: MutableMap<String, String>) {
        if (card.type == null) errors["$path.type"] = "must not be null"
        if (card.questionText.isNullOrBlank()) errors["$path.questionText"] = "must not be blank"

        val timer = card.timerSeconds
        when {
            timer == null -> errors["$path.timerSeconds"] = "must not be null"
            timer <= 0 -> errors["$path.timerSeconds"] = "must be greater than 0"
        }

        when (card.type) {
            CardType.SINGLE_CHOICE, CardType.MULTIPLE_CHOICE -> {
                val options = card.options
                if (options.isNullOrEmpty()) {
                    errors["$path.options"] = "must not be empty"
                } else {
                    options.forEachIndexed { j, opt ->
                        if (opt.text.isBlank()) errors["$path.options[$j].text"] = "must not be blank"
                    }
                    if (options.none { it.isCorrect }) errors["$path.options"] = "at least one option must be correct"
                }
                if (!card.acceptedTexts.isNullOrEmpty()) errors["$path.acceptedTexts"] =
                    "must be empty for choice cards"
            }

            CardType.TEXT_INPUT -> {
                val accepted = card.acceptedTexts
                if (accepted.isNullOrEmpty()) {
                    errors["$path.acceptedTexts"] = "must not be empty"
                } else {
                    accepted.forEachIndexed { j, t ->
                        if (t.isBlank()) errors["$path.acceptedTexts[$j]"] = "must not be blank"
                    }
                }
                if (!card.options.isNullOrEmpty()) errors["$path.options"] = "must be empty for text cards"
            }

            null -> Unit
        }
    }


    private fun findOrThrow(ownerId: UUID, quizId: UUID): QuizEntity {
        val quiz = quizRepository.findById(quizId) ?: throw EntityNotFoundException("Quiz")
        if (quiz.ownerId != ownerId) throw PermissionDeniedException()
        return quiz
    }

    private fun QuizEntity.toDto() = QuizDto(
        id = id,
        ownerId = ownerId,
        visibility = visibility,
        status = if (snapshot != null) QuizStatus.PUBLISHED else QuizStatus.UNPUBLISHED,
        draft = draft?.let { QuizEntityMapperImpl.toContent(it) },
        snapshot = snapshot?.let { QuizEntityMapperImpl.toContent(it) },
    )

    private fun QuizEntity.toSummaries(): List<QuizSummaryDto> = listOfNotNull(
        draft?.let {
            QuizSummaryDto(
                id,
                it.title,
                it.description,
                QuizStatus.UNPUBLISHED,
                visibility,
                it.cards.size,
                it.modifiedAt,
                it.createdAt
            )
        },
        snapshot?.let {
            QuizSummaryDto(
                id,
                it.title,
                it.description,
                QuizStatus.PUBLISHED,
                visibility,
                it.cards.size,
                it.modifiedAt,
                it.createdAt
            )
        },
    )
}
