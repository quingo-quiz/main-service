package tech.arhr.quingo.support

import io.quarkus.cache.CacheManager
import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.common.QuarkusTestResource
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import tech.arhr.quingo.persistence.repository.QuizRepository
import java.util.UUID


@QuarkusTestResource(PostgresRedisTestResource::class)
abstract class IntegrationTestBase {

    @Inject
    lateinit var quizRepository: QuizRepository

    @Inject
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun resetState() {
        RestAssured.basePath = "/api"
        QuarkusTransaction.requiringNew().run {
            quizRepository.listAll().forEach { quizRepository.delete(it) }
        }
        cacheManager.cacheNames.forEach { name ->
            cacheManager.getCache(name).ifPresent { it.invalidateAll().await().indefinitely() }
        }
    }

    /** RestAssured-спецификация от имени пользователя с обязательными заголовками шлюза. */
    protected fun givenUser(
        userId: UUID,
        sessionId: UUID = UUID.randomUUID(),
    ): RequestSpecification =
        RestAssured.given()
            .header("X-User-Id", userId.toString())
            .header("X-Session-Id", sessionId.toString())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)

    /** Создаёт квиз и возвращает его id. */
    protected fun createQuiz(userId: UUID, title: String = "My quiz", visibility: String = "PUBLIC"): UUID =
        UUID.fromString(
            givenUser(userId)
                .body(TestFixtures.createQuizBody(title, visibility))
                .post("/quizzes")
                .then().statusCode(201)
                .extract().path("data.id"),
        )

    /** Полный флоу до публикации: create → сохранить валидный черновик → publish. Возвращает id. */
    protected fun createPublishedQuiz(userId: UUID, title: String = "My quiz", visibility: String = "PUBLIC"): UUID {
        val id = createQuiz(userId, title, visibility)
        givenUser(userId).body(TestFixtures.validDraftBody(title)).put("/quizzes/$id/draft").then().statusCode(200)
        givenUser(userId).post("/quizzes/$id/publish").then().statusCode(200)
        return id
    }
}
