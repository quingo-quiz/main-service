package tech.arhr.quingo.api.rest

import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tech.arhr.quingo.support.IntegrationTestBase
import tech.arhr.quingo.support.TestFixtures
import java.util.UUID

@QuarkusTest
class ValidationTest : IntegrationTestBase() {

    private val user: UUID = UUID.randomUUID()

    @Test
    fun `create rejects blank title`() {
        givenUser(user).body(TestFixtures.createQuizBody(title = "", visibility = "PUBLIC"))
            .post("/quizzes")
            .then().statusCode(400)
            .body("status", equalTo(400))
            .body("fieldErrors", notNullValue())
    }

    @Test
    fun `create rejects null visibility`() {
        givenUser(user).body("""{"title":"ok","visibility":null}""")
            .post("/quizzes")
            .then().statusCode(400)
            .body("fieldErrors", notNullValue())
    }

    @Test
    fun `create rejects unknown enum value`() {
        givenUser(user).body("""{"title":"ok","visibility":"PURPLE"}""")
            .post("/quizzes")
            .then().statusCode(400)
            .body("fieldErrors.visibility", equalTo("must be one of: PUBLIC, PRIVATE"))
    }

    @Test
    fun `create rejects malformed json`() {
        givenUser(user).body("{ not json")
            .post("/quizzes")
            .then().statusCode(400)
    }

    @Test
    fun `publish incomplete quiz returns field errors`() {
        val id = createQuiz(user)
        givenUser(user).body(TestFixtures.incompleteDraftBody()).put("/quizzes/$id/draft").then().statusCode(200)

        val fieldErrors = givenUser(user).post("/quizzes/$id/publish")
            .then().statusCode(400)
            .extract().jsonPath().getMap<String, String>("fieldErrors")

        assertNotNull(fieldErrors)
        assertTrue(fieldErrors.keys.any { it.startsWith("cards[0]") }, "expected per-card errors, got $fieldErrors")
    }

    @Test
    fun `unknown quiz returns 404 error envelope`() {
        givenUser(user).get("/quizzes/${UUID.randomUUID()}")
            .then().statusCode(404)
            .body("status", equalTo(404))
            .body("method", equalTo("GET"))
            .body("path", notNullValue())
            .body("timestamp", notNullValue())
    }
}
