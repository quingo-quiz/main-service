package tech.arhr.quingo.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tech.arhr.quingo.api.rest.models.quiz.CardInput
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.QuizConstraints
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.common.TimeProvider
import tech.arhr.quingo.dto.CardType
import tech.arhr.quingo.dto.QuizDto
import tech.arhr.quingo.dto.QuizStatus
import tech.arhr.quingo.dto.QuizSummaryDto
import tech.arhr.quingo.dto.Visibility
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
        val quiz = QuizEntity().apply {
            this.ownerId = ownerId
            this.visibility = request.visibility!!
        }
        val now = time.now()
        val draft = QuizDraftEntity().apply {
            this.quiz = quiz
            this.title = request.title
            this.description = request.description
            this.createdAt = now
            this.modifiedAt = now
        }
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
        val draft = QuizDraftEntity().apply {
            this.quiz = quiz
            this.title = snapshot.title
            this.description = snapshot.description
            this.createdAt = now
            this.modifiedAt = now
        }
        draft.cards = snapshot.cards.map { src ->
            CardEntity().apply { this.draft = draft }.also { src.copyInto(it, src.position) }
        }.toMutableList()
        quiz.draft = draft
        return quiz.toDto()
    }

    override fun saveDraft(ownerId: UUID, quizId: UUID, request: SaveDraftRequest): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        val draft = quiz.draft ?: throw QuingoAppException("Quiz has no draft")

        draft.title = request.title
        draft.description = request.description
        draft.modifiedAt = time.now()
        reconcileCards(draft.cards, request.cards!!, { CardEntity().apply { this.draft = draft } }) { target, input, pos ->
            input.applyTo(target, pos)
        }

        return quiz.toDto()
    }

    override fun publish(ownerId: UUID, quizId: UUID): QuizDto {
        val quiz = findOrThrow(ownerId, quizId)
        val draft = quiz.draft ?: throw QuingoAppException("Quiz has no draft to publish")
        validateForPublish(draft)
        val now = time.now()

        val snapshot = quiz.snapshot ?: QuizSnapshotEntity().also { s ->
            s.quiz = quiz
            s.createdAt = now
            quiz.snapshot = s
        }
        snapshot.title = draft.title!!
        snapshot.description = draft.description
        snapshot.modifiedAt = now
        reconcileCards(snapshot.cards, draft.cards, { CardEntity().apply { this.snapshot = snapshot } }) { target, src, pos ->
            src.copyInto(target, pos)
        }

        quiz.draft = null
        return quiz.toDto()
    }

    override fun deleteDraft(ownerId: UUID, quizId: UUID) {
        val quiz = findOrThrow(ownerId, quizId)
        if (quiz.draft == null) throw QuingoAppException("Quiz has no draft")
        if (quiz.snapshot == null) {
            quizRepository.delete(quiz)
            return
        }
        quiz.draft = null
    }

    // --- helpers ---

    /**
     * Полная проверка черновика перед публикацией. В отличие от мягкой валидации
     * при сохранении черновика, здесь требуется завершённость: непустой заголовок,
     * хотя бы одна карточка, заданный тип и текст у каждой карточки, согласованность
     * ответа с типом. Ошибки собираются по полям в нотации `cards[0].questionText`.
     */
    private fun validateForPublish(draft: QuizDraftEntity) {
        val errors = LinkedHashMap<String, String>()

        if (draft.title.isNullOrBlank()) {
            errors["title"] = "must not be blank"
        }
        if (draft.cards.isEmpty()) {
            errors["cards"] = "must contain at least one card"
        }
        draft.cards.forEachIndexed { i, card -> validateCardForPublish(card, "cards[$i]", errors) }

        if (errors.isNotEmpty()) {
            throw QuingoAppException("Cannot publish: quiz is incomplete", Response.Status.BAD_REQUEST, errors)
        }
    }

    private fun validateCardForPublish(card: CardEntity, path: String, errors: MutableMap<String, String>) {
        val type = card.type
        if (type == null) errors["$path.type"] = "must not be null"

        if (card.questionText.isNullOrBlank()) errors["$path.questionText"] = "must not be blank"

        val timer = card.timerSeconds
        if (timer == null) errors["$path.timerSeconds"] = "must not be null"
        else if (timer <= 0) errors["$path.timerSeconds"] = "must be greater than 0"

        when (type) {
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
                if (!card.acceptedTexts.isNullOrEmpty()) errors["$path.acceptedTexts"] = "must be empty for choice cards"
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
            null -> Unit // тип уже зафиксирован как ошибка выше
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

    private fun QuizEntity.toSummaries(): List<QuizSummaryDto> = buildList {
        draft?.let { add(QuizSummaryDto(id, it.title, it.description, QuizStatus.UNPUBLISHED, visibility, it.cards.size, it.modifiedAt, it.createdAt)) }
        snapshot?.let { add(QuizSummaryDto(id, it.title, it.description, QuizStatus.PUBLISHED, visibility, it.cards.size, it.modifiedAt, it.createdAt)) }
    }

    /**
     * Синхронизирует список карточек-сущностей с источником по позициям: существующие
     * обновляются на месте, лишние удаляются с конца (orphanRemoval), недостающие создаются.
     * Позиции не «съезжают», поэтому uq_*_position не нарушается и flush не требуется.
     */
    private fun <S> reconcileCards(
        existing: MutableList<CardEntity>,
        sources: List<S>,
        newCard: () -> CardEntity,
        apply: (target: CardEntity, source: S, position: Int) -> Unit,
    ) {
        while (existing.size > sources.size) {
            existing.removeAt(existing.size - 1)
        }
        sources.forEachIndexed { i, source ->
            val card = if (i < existing.size) existing[i] else newCard().also { existing.add(it) }
            apply(card, source, i)
        }
    }

    private fun CardInput.applyTo(target: CardEntity, position: Int) {
        target.position = position
        target.type = this.type
        target.questionText = this.questionText
        target.timerSeconds = this.timerSeconds
        target.options = this.options?.takeIf { it.isNotEmpty() }?.mapIndexed { i, opt ->
            OptionJson(i, opt.text ?: "", opt.isCorrect ?: false)
        }?.toMutableList()
        target.acceptedTexts = this.acceptedTexts?.takeIf { it.isNotEmpty() }?.toMutableList()
    }

    private fun CardEntity.copyInto(target: CardEntity, position: Int) {
        target.position = position
        target.type = this.type
        target.questionText = this.questionText
        target.timerSeconds = this.timerSeconds
        target.options = this.options?.toMutableList()
        target.acceptedTexts = this.acceptedTexts?.toMutableList()
    }
}
