package Counter;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void testDatabaseConnection() {
        try (PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")) {
            postgresContainer.start();

            String jdbcUrl = postgresContainer.getJdbcUrl();
            String username = postgresContainer.getUsername();
            String password = postgresContainer.getPassword();

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                assertTrue(connection.isValid(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}