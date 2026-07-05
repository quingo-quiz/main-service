package tech.arhr.quingo.service

import io.quarkus.cache.CacheManager
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import tech.arhr.quingo.dto.quiz.Visibility
import tech.arhr.quingo.exceptions.EntityNotFoundException
import tech.arhr.quingo.persistence.entity.QuizEntity
import tech.arhr.quingo.persistence.entity.QuizSnapshotEntity
import tech.arhr.quingo.persistence.repository.QuizRepository
import tech.arhr.quingo.support.PostgresRedisTestResource
import java.time.Instant
import java.util.UUID

@QuarkusTest
@QuarkusTestResource(PostgresRedisTestResource::class)
class CatalogServiceImplTest {

    @Inject
    lateinit var service: CatalogService

    @InjectMock
    lateinit var repository: QuizRepository

    @Inject
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun setUp() {
        cacheManager.cacheNames.forEach { name ->
            cacheManager.getCache(name).ifPresent { it.invalidateAll().await().indefinitely() }
        }
    }

    // --- search: пагинация и маппинг ---

    @Test
    fun `search applies defaults and maps items`() {
        Mockito.`when`(repository.countCatalogItems(null)).thenReturn(3L)
        Mockito.`when`(repository.searchCatalog(null, 0, 20)).thenReturn(listOf(publicQuiz("A")))

        val dto = service.search(null, null, null)

        assertEquals(0, dto.page)
        assertEquals(20, dto.size)
        assertEquals(3L, dto.total)
        assertEquals(1, dto.totalPages)
        assertEquals(1, dto.items.size)
        assertEquals("A", dto.items.first().title)
    }

    @Test
    fun `search coerces negative page to zero`() {
        Mockito.`when`(repository.countCatalogItems(null)).thenReturn(0L)
        Mockito.`when`(repository.searchCatalog(null, 0, 20)).thenReturn(emptyList())

        val dto = service.search(null, -5, 20)

        assertEquals(0, dto.page)
    }

    @Test
    fun `search coerces too small size to one`() {
        Mockito.`when`(repository.countCatalogItems(null)).thenReturn(0L)
        Mockito.`when`(repository.searchCatalog(null, 0, 1)).thenReturn(emptyList())

        val dto = service.search(null, 0, 0)

        assertEquals(1, dto.size)
    }

    @Test
    fun `search coerces too large size to hundred`() {
        Mockito.`when`(repository.countCatalogItems(null)).thenReturn(0L)
        Mockito.`when`(repository.searchCatalog(null, 0, 100)).thenReturn(emptyList())

        val dto = service.search(null, 0, 500)

        assertEquals(100, dto.size)
    }

    @Test
    fun `search computes total pages by ceiling`() {
        Mockito.`when`(repository.countCatalogItems("q")).thenReturn(25L)
        Mockito.`when`(repository.searchCatalog("q", 0, 10)).thenReturn(emptyList())

        val dto = service.search("q", 0, 10)

        assertEquals(3, dto.totalPages)
    }

    // --- getById ---

    @Test
    fun `getById returns public published item`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(publicQuiz("Title", id))

        val item = service.getById(id)

        assertEquals(id, item.id)
        assertEquals("Title", item.title)
    }

    @Test
    fun `getById throws when missing`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(null)

        assertThrows(EntityNotFoundException::class.java) { service.getById(id) }
    }

    @Test
    fun `getById throws when private`() {
        val id = UUID.randomUUID()
        Mockito.`when`(repository.findById(id)).thenReturn(publicQuiz("Title", id, visibility = Visibility.PRIVATE))

        assertThrows(EntityNotFoundException::class.java) { service.getById(id) }
    }

    @Test
    fun `getById throws when not published`() {
        val id = UUID.randomUUID()
        val quiz = QuizEntity().also {
            it.id = id
            it.ownerId = UUID.randomUUID()
            it.visibility = Visibility.PUBLIC
        }
        Mockito.`when`(repository.findById(id)).thenReturn(quiz)

        assertThrows(EntityNotFoundException::class.java) { service.getById(id) }
    }

    // --- helpers ---

    private fun publicQuiz(
        title: String,
        id: UUID = UUID.randomUUID(),
        visibility: Visibility = Visibility.PUBLIC,
    ): QuizEntity {
        val quiz = QuizEntity().also {
            it.id = id
            it.ownerId = UUID.randomUUID()
            it.visibility = visibility
        }
        quiz.snapshot = QuizSnapshotEntity().also {
            it.quiz = quiz
            it.title = title
            it.createdAt = NOW
            it.modifiedAt = NOW
        }
        return quiz
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-01-01T00:00:00Z")
    }
}
