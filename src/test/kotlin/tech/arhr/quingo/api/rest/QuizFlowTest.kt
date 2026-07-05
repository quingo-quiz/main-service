package tech.arhr.quingo.api.rest

import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import tech.arhr.quingo.support.IntegrationTestBase
import tech.arhr.quingo.support.TestFixtures
import java.util.UUID

@QuarkusTest
class QuizFlowTest : IntegrationTestBase() {

    private val user: UUID = UUID.randomUUID()

    @Test
    fun `full lifecycle create edit publish`() {
        val id = createQuiz(user, title = "Lifecycle")

        givenUser(user).get("/quizzes/$id")
            .then().statusCode(200)
            .body("data.status", equalTo("UNPUBLISHED"))
            .body("data.draft", notNullValue())
            .body("data.snapshot", nullValue())

        givenUser(user).body(TestFixtures.validDraftBody("Lifecycle"))
            .put("/quizzes/$id/draft")
            .then().statusCode(200)

        givenUser(user).post("/quizzes/$id/publish")
            .then().statusCode(200)
            .body("data.status", equalTo("PUBLISHED"))
            .body("data.snapshot", notNullValue())
            .body("data.snapshot.cards.size()", equalTo(3))
            .body("data.draft", nullValue())
    }

    @Test
    fun `create draft from snapshot and republish updates snapshot`() {
        val id = createPublishedQuiz(user, title = "V1")

        givenUser(user).post("/quizzes/$id/draft")
            .then().statusCode(201)
            .body("data.draft", notNullValue())

        givenUser(user).body(TestFixtures.draftBody("V2", TestFixtures.TEXT_INPUT_CARD))
            .put("/quizzes/$id/draft")
            .then().statusCode(200)

        givenUser(user).post("/quizzes/$id/publish")
            .then().statusCode(200)
            .body("data.snapshot.title", equalTo("V2"))
            .body("data.snapshot.cards.size()", equalTo(1))
    }

    @Test
    fun `deleteDraft removes quiz when there is no snapshot`() {
        val id = createQuiz(user)

        givenUser(user).delete("/quizzes/$id/draft").then().statusCode(200)
        givenUser(user).get("/quizzes/$id").then().statusCode(404)
    }

    @Test
    fun `deleteDraft keeps published quiz`() {
        val id = createPublishedQuiz(user)

        // создаём черновик поверх снапшота, затем удаляем только его
        givenUser(user).post("/quizzes/$id/draft").then().statusCode(201)
        givenUser(user).delete("/quizzes/$id/draft").then().statusCode(200)

        givenUser(user).get("/quizzes/$id")
            .then().statusCode(200)
            .body("data.status", equalTo("PUBLISHED"))
            .body("data.snapshot", notNullValue())
            .body("data.draft", nullValue())
    }

    @Test
    fun `delete quiz then get returns 404`() {
        val id = createQuiz(user)

        givenUser(user).delete("/quizzes/$id").then().statusCode(200)
        givenUser(user).get("/quizzes/$id").then().statusCode(404)
    }

    @Test
    fun `list returns draft and snapshot summaries`() {
        val id = createPublishedQuiz(user)
        givenUser(user).post("/quizzes/$id/draft").then().statusCode(201)

        givenUser(user).get("/quizzes")
            .then().statusCode(200)
            .body("data.size()", equalTo(2))
    }
}
