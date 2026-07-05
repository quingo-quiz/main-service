package tech.arhr.quingo.support

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Поднимает PostgreSQL и Redis в Testcontainers и подменяет соответствующие
 * настройки [application.yml]. Ресурс глобальный — стартует один раз на весь прогон тестов.
 */
class PostgresRedisTestResource : QuarkusTestResourceLifecycleManager {

    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("main-service-db")
        .withUsername("test")
        .withPassword("test")

    private val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(REDIS_PORT)

    override fun start(): Map<String, String> {
        postgres.start()
        redis.start()
        return mapOf(
            "quarkus.datasource.jdbc.url" to postgres.jdbcUrl,
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "quarkus.redis.hosts" to "redis://${redis.host}:${redis.getMappedPort(REDIS_PORT)}",
        )
    }

    override fun stop() {
        redis.stop()
        postgres.stop()
    }

    private companion object {
        const val REDIS_PORT = 6379
    }
}
