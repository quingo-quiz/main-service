package tech.arhr.quingo.service

import io.quarkus.cache.CacheManager
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import tech.arhr.quingo.api.rest.models.quiz.CardInput
import tech.arhr.quingo.api.rest.models.quiz.CardOptionInput
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.common.TimeProvider
import tech.arhr.quingo.dto.quiz.CardType
import tech.arhr.quingo.dto.quiz.QuizStatus
import tech.arhr.quingo.dto.quiz.Visibility
import tech.arhr.quingo.exceptions.EntityNotFoundException
import tech.arhr.quingo.exceptions.PermissionDeniedException
import tech.arhr.quingo.exceptions.QuingoAppException
import tech.arhr.quingo.persistence.entity.CardEntity
import tech.arhr.quingo.persistence.entity.OptionJson
import tech.arhr.quingo.persistence.entity.QuizDraftEntity
import tech.arhr.quingo.persistence.entity.QuizEntity
import tech.arhr.quingo.persistence.entity.QuizSnapshotEntity
import tech.arhr.quingo.persistence.repository.QuizRepository
import tech.arhr.quingo.support.PostgresRedisTestResource
import java.time.Instant
import java.util.UUID

@QuarkusTest
@QuarkusTestResource(PostgresRedisTestResource::class)
class QuizServiceImplTest {

    @Inject
    lateinit var service: QuizService

    @InjectMock
    lateinit var repository: QuizRepository

    @InjectMock
    lateinit var time: TimeProvider

    @Inject
    lateinit var cacheManager: CacheManager

    private val owner: UUID = UUID.randomUUID()
    private val stranger: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        Mockito.`when`(time.now()).thenReturn(NOW)
        cacheManager.cacheNames.forEach { name ->
            cacheManager.getCache(name).ifPresent { it.invalidateAll().await().indefinitely() }
        }
    }

    // --- createQuiz ---

    @Test
    fun `createQuiz builds unpublished quiz with draft and persists`() {
        val request = CreateQuizRequest(title = "Title", description = "Desc", visibility = Visibility.PUBLIC)

        val dto = service.createQuiz(owner, request)

        assertEquals(owner, dto.ownerId)
        assertEquals(Visibility.PUBLIC, dto.visibility)
        assertEquals(QuizStatus.UNPUBLISHED, dto.status)
        assertNotNull(dto.draft)
        assertEquals("Title", dto.draft!!.title)
        assertEquals(NOW, dto.draft!!.createdAt)
        assertNull(dto.snapshot)
        Mockito.verify(repository).persist(anyQuiz())
    }

    // --- get ---

    @Test
    fun `get returns dto for owner`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        val dto = service.get(owner, id)

        assertEquals(id, dto.id)
        assertEquals(owner, dto.ownerId)
    }

    @Test
    fun `get throws EntityNotFound when missing`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(null)

        val ex = assertThrows(EntityNotFoundException::class.java) { service.get(owner, id) }
        assertEquals(404, ex.status.statusCode)
    }

    @Test
    fun `get throws PermissionDenied for foreign owner`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        val ex = assertThrows(PermissionDeniedException::class.java) { service.get(stranger, id) }
        assertEquals(403, ex.status.statusCode)
    }

    // --- update ---

    @Test
    fun `update changes visibility for owner`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id, visibility = Visibility.PRIVATE))

        val dto = service.update(owner, id, Visibility.PUBLIC)

        assertEquals(Visibility.PUBLIC, dto.visibility)
    }

    @Test
    fun `update rejects foreign owner`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        assertThrows(PermissionDeniedException::class.java) { service.update(stranger, id, Visibility.PUBLIC) }
    }

    // --- delete ---

    @Test
    fun `delete removes quiz of owner`() {
        val id = UUID.randomUUID()
        val entity = quiz(id)
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        service.delete(owner, id)

        Mockito.verify(repository).delete(entity)
    }

    @Test
    fun `delete rejects foreign owner`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        assertThrows(PermissionDeniedException::class.java) { service.delete(stranger, id) }
    }

    // --- createDraft ---

    @Test
    fun `createDraft fails without snapshot`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        assertThrows(QuingoAppException::class.java) { service.createDraft(owner, id) }
    }

    @Test
    fun `createDraft fails when draft already exists`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply { draft = draft(this, "d") }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        assertThrows(QuingoAppException::class.java) { service.createDraft(owner, id) }
    }

    @Test
    fun `createDraft copies snapshot cards into new draft`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply { snapshot = snapshot(this, "Published", cards = 2) }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        val dto = service.createDraft(owner, id)

        assertNotNull(dto.draft)
        assertEquals("Published", dto.draft!!.title)
        assertEquals(2, dto.draft!!.cards.size)
    }

    // --- saveDraft ---

    @Test
    fun `saveDraft fails without draft`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        val request = SaveDraftRequest(title = "t", description = null, cards = emptyList())
        assertThrows(QuingoAppException::class.java) { service.saveDraft(owner, id, request) }
    }

    @Test
    fun `saveDraft replaces cards and title`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply { draft = draft(this, "old") }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        val request = SaveDraftRequest(
            title = "new",
            description = "d",
            cards = listOf(
                CardInput(
                    type = CardType.SINGLE_CHOICE,
                    questionText = "Q",
                    timerSeconds = 10,
                    options = listOf(CardOptionInput("A", true)),
                ),
            ),
        )

        val dto = service.saveDraft(owner, id, request)

        assertEquals("new", dto.draft!!.title)
        assertEquals(1, dto.draft!!.cards.size)
    }

    // --- publish (happy) ---

    @Test
    fun `publish creates snapshot and clears draft`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply { draft = validDraft(this) }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        val dto = service.publish(owner, id)

        assertEquals(QuizStatus.PUBLISHED, dto.status)
        assertNotNull(dto.snapshot)
        assertNull(dto.draft)
    }

    @Test
    fun `publish fails without draft`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        assertThrows(QuingoAppException::class.java) { service.publish(owner, id) }
    }

    // --- publish validation matrix ---

    @Test
    fun `publish rejects blank title`() {
        val errors = publishExpectingErrors(draftTitle = null, cards = listOf(validCard()))
        assertTrue(errors.containsKey("title"), "expected title error, got $errors")
    }

    @Test
    fun `publish rejects empty cards`() {
        val errors = publishExpectingErrors(draftTitle = "ok", cards = emptyList())
        assertTrue(errors.containsKey("cards"), "expected cards error, got $errors")
    }

    @Test
    fun `publish reports null type, blank question and missing timer`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(card(type = null, question = null, timer = null)),
        )
        assertTrue(errors.containsKey("cards[0].type"))
        assertTrue(errors.containsKey("cards[0].questionText"))
        assertTrue(errors.containsKey("cards[0].timerSeconds"))
    }

    @Test
    fun `publish rejects non-positive timer`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(card(type = CardType.TEXT_INPUT, question = "Q", timer = 0, accepted = listOf("a"))),
        )
        assertEquals("must be greater than 0", errors["cards[0].timerSeconds"])
    }

    @Test
    fun `publish rejects choice card without correct option`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(
                card(
                    type = CardType.SINGLE_CHOICE, question = "Q", timer = 10,
                    options = listOf(OptionJson(0, "A", false), OptionJson(1, "B", false)),
                ),
            ),
        )
        assertEquals("at least one option must be correct", errors["cards[0].options"])
    }

    @Test
    fun `publish rejects choice card carrying accepted texts`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(
                card(
                    type = CardType.SINGLE_CHOICE, question = "Q", timer = 10,
                    options = listOf(OptionJson(0, "A", true)),
                    accepted = listOf("x"),
                ),
            ),
        )
        assertTrue(errors.containsKey("cards[0].acceptedTexts"))
    }

    @Test
    fun `publish rejects text card without accepted texts`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(card(type = CardType.TEXT_INPUT, question = "Q", timer = 10, accepted = emptyList())),
        )
        assertTrue(errors.containsKey("cards[0].acceptedTexts"))
    }

    @Test
    fun `publish rejects text card carrying options`() {
        val errors = publishExpectingErrors(
            draftTitle = "ok",
            cards = listOf(
                card(
                    type = CardType.TEXT_INPUT, question = "Q", timer = 10,
                    accepted = listOf("a"),
                    options = listOf(OptionJson(0, "A", true)),
                ),
            ),
        )
        assertTrue(errors.containsKey("cards[0].options"))
    }

    // --- deleteDraft ---

    @Test
    fun `deleteDraft fails without draft`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(quiz(id))

        assertThrows(QuingoAppException::class.java) { service.deleteDraft(owner, id) }
    }

    @Test
    fun `deleteDraft deletes quiz when no snapshot`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply { draft = draft(this, "d") }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        service.deleteDraft(owner, id)

        Mockito.verify(repository).delete(entity)
    }

    @Test
    fun `deleteDraft keeps quiz and drops draft when snapshot exists`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply {
            draft = draft(this, "d")
            snapshot = snapshot(this, "Published", cards = 1)
        }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        service.deleteDraft(owner, id)

        Mockito.verify(repository, Mockito.never()).delete(entity)
        assertNull(entity.draft)
    }

    // --- listSummaries ---

    @Test
    fun `listSummaries maps draft and snapshot`() {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply {
            draft = draft(this, "draft")
            snapshot = snapshot(this, "snap", cards = 1)
        }
        Mockito.`when`(repository.findByOwner(owner)).thenReturn(listOf(entity))

        val summaries = service.listSummaries(owner)

        assertEquals(2, summaries.size)
        assertTrue(summaries.any { it.status == QuizStatus.UNPUBLISHED })
        assertTrue(summaries.any { it.status == QuizStatus.PUBLISHED })
    }

    // --- helpers ---

    private fun publishExpectingErrors(draftTitle: String?, cards: List<CardEntity>): Map<String, String> {
        val id = UUID.randomUUID()
        val entity = quiz(id).apply {
            draft = QuizDraftEntity().also {
                it.quiz = this
                it.title = draftTitle
                it.createdAt = NOW
                it.modifiedAt = NOW
                it.cards = cards.toMutableList()
            }
        }
        Mockito.`when`(repository.findById(id)).thenReturn(entity)

        val ex = assertThrows(QuingoAppException::class.java) { service.publish(owner, id) }
        assertEquals(400, ex.status.statusCode)
        return ex.fieldErrors ?: emptyMap()
    }

    private fun quiz(id: UUID, ownerId: UUID = owner, visibility: Visibility = Visibility.PRIVATE): QuizEntity =
        QuizEntity().also {
            it.id = id
            it.ownerId = ownerId
            it.visibility = visibility
        }

    private fun draft(quiz: QuizEntity, title: String?): QuizDraftEntity =
        QuizDraftEntity().also {
            it.quiz = quiz
            it.title = title
            it.createdAt = NOW
            it.modifiedAt = NOW
        }

    private fun snapshot(quiz: QuizEntity, title: String, cards: Int): QuizSnapshotEntity =
        QuizSnapshotEntity().also { snap ->
            snap.quiz = quiz
            snap.title = title
            snap.createdAt = NOW
            snap.modifiedAt = NOW
            snap.cards = (0 until cards).map { i ->
                validCard().also { it.snapshot = snap; it.position = i }
            }.toMutableList()
        }

    private fun validDraft(quiz: QuizEntity): QuizDraftEntity =
        draft(quiz, "Draft").also { d ->
            d.cards = mutableListOf(validCard().also { it.draft = d; it.position = 0 })
        }

    private fun validCard(): CardEntity =
        card(type = CardType.SINGLE_CHOICE, question = "Q", timer = 10, options = listOf(OptionJson(0, "A", true)))

    private fun card(
        type: CardType?,
        question: String?,
        timer: Int?,
        options: List<OptionJson>? = null,
        accepted: List<String>? = null,
    ): CardEntity = CardEntity().also {
        it.type = type
        it.questionText = question
        it.timerSeconds = timer
        it.options = options?.toMutableList()
        it.acceptedTexts = accepted?.toMutableList()
    }

    private fun anyQuiz(): QuizEntity = Mockito.any(QuizEntity::class.java) ?: QuizEntity()

    private companion object {
        val NOW: Instant = Instant.parse("2026-01-01T00:00:00Z")
    }
}
