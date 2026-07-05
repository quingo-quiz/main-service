package tech.arhr.quingo.api.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import tech.arhr.quingo.support.IntegrationTestBase
import tech.arhr.quingo.support.TestFixtures
import java.util.UUID

@QuarkusTest
class QuizSecurityTest : IntegrationTestBase() {

    private val alice: UUID = UUID.randomUUID()
    private val bob: UUID = UUID.randomUUID()

    @Test
    fun `foreign user cannot read another users quiz`() {
        val id = createQuiz(alice)

        givenUser(bob).get("/quizzes/$id").then().statusCode(403)
    }

    @Test
    fun `foreign user cannot update another users quiz`() {
        val id = createQuiz(alice)

        givenUser(bob).body(TestFixtures.updateVisibilityBody("PUBLIC"))
            .patch("/quizzes/$id")
            .then().statusCode(403)
    }

    @Test
    fun `foreign user cannot delete another users quiz`() {
        val id = createQuiz(alice)

        givenUser(bob).delete("/quizzes/$id").then().statusCode(403)
    }

    @Test
    fun `missing user id header is rejected`() {
        RestAssured.given()
            .header("X-Session-Id", UUID.randomUUID().toString())
            .accept(ContentType.JSON)
            .get("/quizzes")
            .then().statusCode(500)
    }
}
