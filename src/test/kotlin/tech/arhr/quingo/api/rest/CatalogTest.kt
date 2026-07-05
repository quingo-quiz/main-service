package tech.arhr.quingo.api.rest

import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import tech.arhr.quingo.support.IntegrationTestBase
import tech.arhr.quingo.support.TestFixtures
import java.util.UUID

@QuarkusTest
class CatalogTest : IntegrationTestBase() {

    private val user: UUID = UUID.randomUUID()

    @Test
    fun `catalog lists only public published quizzes`() {
        createPublishedQuiz(user, title = "Public one", visibility = "PUBLIC")
        createPublishedQuiz(user, title = "Private one", visibility = "PRIVATE")
        createQuiz(user, title = "Unpublished", visibility = "PUBLIC") // только черновик

        givenUser(user).get("/catalog")
            .then().statusCode(200)
            .body("data.total", equalTo(1))
            .body("data.items.size()", equalTo(1))
            .body("data.items[0].title", equalTo("Public one"))
    }

    @Test
    fun `catalog search matches by title`() {
        createPublishedQuiz(user, title = "Kotlin basics", visibility = "PUBLIC")
        createPublishedQuiz(user, title = "Java basics", visibility = "PUBLIC")

        givenUser(user).queryParam("q", "kotlin").get("/catalog")
            .then().statusCode(200)
            .body("data.total", equalTo(1))
            .body("data.items[0].title", equalTo("Kotlin basics"))
    }

    @Test
    fun `catalog paginates results`() {
        repeat(3) { createPublishedQuiz(user, title = "Quiz $it", visibility = "PUBLIC") }

        givenUser(user).queryParam("page", 0).queryParam("size", 2).get("/catalog")
            .then().statusCode(200)
            .body("data.total", equalTo(3))
            .body("data.totalPages", equalTo(2))
            .body("data.items.size()", equalTo(2))
    }

    @Test
    fun `getById returns public published item`() {
        val id = createPublishedQuiz(user, title = "Item", visibility = "PUBLIC")

        givenUser(user).get("/catalog/$id")
            .then().statusCode(200)
            .body("data.id", equalTo(id.toString()))
            .body("data.title", equalTo("Item"))
    }

    @Test
    fun `getById hides private quiz`() {
        val id = createPublishedQuiz(user, visibility = "PRIVATE")

        givenUser(user).get("/catalog/$id").then().statusCode(404)
    }

    @Test
    fun `getById hides unpublished quiz`() {
        val id = createQuiz(user, visibility = "PUBLIC")

        givenUser(user).get("/catalog/$id").then().statusCode(404)
    }

    @Test
    fun `getById returns 404 for unknown id`() {
        givenUser(user).get("/catalog/${UUID.randomUUID()}").then().statusCode(404)
    }

    @Test
    fun `getById reflects republished changes (item cache point-invalidation)`() {
        val id = createPublishedQuiz(user, title = "OrigTitle", visibility = "PUBLIC")

        // прогреваем item-кэш
        givenUser(user).get("/catalog/$id").then().statusCode(200).body("data.title", equalTo("OrigTitle"))

        // правим и переопубликовываем
        givenUser(user).post("/quizzes/$id/draft").then().statusCode(201)
        givenUser(user).body(TestFixtures.draftBody("NewTitle", TestFixtures.TEXT_INPUT_CARD))
            .put("/quizzes/$id/draft").then().statusCode(200)
        givenUser(user).post("/quizzes/$id/publish").then().statusCode(200)

        // publish точечно инвалидирует catalog-item-cache → свежие данные
        givenUser(user).get("/catalog/$id").then().statusCode(200).body("data.title", equalTo("NewTitle"))
    }
}
