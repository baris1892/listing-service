package dev.baristop.portfolio.listingservice.utils;

import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests.
 * <p>
 * Starts PostgreSQL and Redis Testcontainers once per test suite.
 * <p>
 * Use @DynamicPropertySource to inject connection details dynamically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@Testcontainers
public abstract class AbstractIntegrationTest {

    // PostgreSQL Testcontainer
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    // Redis Testcontainer
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withReuse(true);

    static {
        POSTGRES.start();
        REDIS.start();

        runFlywayMigration();
    }

    private static void runFlywayMigration() {
        Flyway flyway = Flyway.configure()
            .dataSource(AbstractIntegrationTest.POSTGRES.getJdbcUrl(), AbstractIntegrationTest.POSTGRES.getUsername(), AbstractIntegrationTest.POSTGRES.getPassword())
            // Match the locations setting from your configuration
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();

        // 🚨 CRITICAL FIX: Clean the database before migration for reuse containers.
        // This ensures the database is pristine, eliminating "random" state bleed.
        try {
            flyway.clean();
        } catch (Exception e) {
            // Ignore potential errors if the schema doesn't exist yet (first run)
            System.out.println("Flyway clean failed (expected on first run): " + e.getMessage());
        }

        flyway.migrate();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        // Redis
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(6379)));
    }
}
