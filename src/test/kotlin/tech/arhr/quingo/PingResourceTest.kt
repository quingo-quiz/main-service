package tech.arhr.quingo

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class PingResourceTest {

    @Test
    fun testPingEndpoint() {
        given()
          .`when`().get("/ping")
          .then()
             .statusCode(200)
             .body(`is`("main-service is up"))
    }

}
