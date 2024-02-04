package Counter;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTest {

    private static final String URL = "jdbc:postgresql://localhost:5432/test";
    private static final String USER_NAME = "test";
    private static final String PASSWORD = "test";

    private static PostgreSQLContainer<?> postgreSQLContainer;

    @BeforeAll
    public static void setUp() {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("test")
                .withUsername(USER_NAME)
                .withPassword(PASSWORD);
        postgreSQLContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        postgreSQLContainer.stop();
    }

    @BeforeEach
    public void initializeDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                postgreSQLContainer.getJdbcUrl(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword())) {
            try (Statement statement = connection.createStatement()) {
                // Создание таблиц и данных для тестов
                statement.execute("CREATE TABLE users (username VARCHAR(255), password VARCHAR(255));");
                statement.execute("INSERT INTO users (username, password) VALUES ('user1', 'pass1');");
            }
        }
    }

    @Test
    public void login() {
        // Подготовка тестовых данных
        Main main = new Main();
        main.saveUser("testuser", "testpassword");

        // Вызов метода login
        main.login("testuser", "testpassword");

        // Проверка, что пользователь успешно вошел
        assertNotNull(main.getCurrentUsername());
    }

    @Test
    public void register() {
        // Подготовка тестовых данных
        Main main = new Main();
        String testUsername = "testuser";
        String testPassword = "testpassword";

        // Вызов метода register
        main.register(testUsername, testPassword);

        // Проверка, что пользователь успешно зарегистрирован
        assertTrue(main.userExists(testUsername));
    }

    @Test
    public void monthlyData() {
        // Подготовка тестовых данных
        Main main = new Main();
        MeterReading currentUserReading = new MeterReading();
        User currentUsername = new User("testuser", "testpassword");

        // Создание объекта ByteArrayOutputStream для захвата вывода
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        // Замена System.out на созданный PrintStream
        PrintStream originalOut = System.out;
        System.setOut(printStream);

        // Подготовка тестовой базы данных в контейнере
        try (Connection connection = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {
            String query = "CREATE TABLE meter_reading (username VARCHAR(255), cold_water_reading INT, hot_water_reading INT, entry_date TIMESTAMP)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ввод месяца для тестирования
        System.setIn(new ByteArrayInputStream("01\n".getBytes()));

        // Вызов метода monthlyData
        main.monthlyData(currentUserReading, currentUsername);

        // Проверка, что вывод не содержит строки об отсутствии данных
        assertFalse(outputStream.toString().contains("No readings available for the specified month"));

        // Проверка, что вывод содержит ожидаемые данные (может потребоваться адаптация)
        assertTrue(outputStream.toString().contains("Readings for"));

        // Восстановление System.out
        System.setOut(originalOut);
    }

    @Test
    public void testDataHistory() {
        // Подготовка тестовых данных
        Main main = new Main();
        MeterReading currentUserReading = new MeterReading();
        User currentUsername = new User("testuser", "testpassword");

        // Создание объекта ByteArrayOutputStream для захвата вывода
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        // Замена System.out на созданный PrintStream
        PrintStream originalOut = System.out;
        System.setOut(printStream);

        // Подготовка тестовой базы данных в контейнере
        try (Connection connection = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {
            String query = "CREATE TABLE meter_reading (username VARCHAR(255), cold_water_reading INT, hot_water_reading INT, entry_date TIMESTAMP)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Вызов метода dataHistory
        main.dataHistory(currentUserReading, currentUsername);

        // Проверка, что вывод содержит сообщение об отсутствии истории данных
        assertTrue(outputStream.toString().contains("No data history available."));

        // Восстановление System.out
        System.setOut(originalOut);
    }

    @Test
    public void testLogout() {
        // Подготовка тестовых данных
        Main main = new Main();
        MeterReading currentUserReading = new MeterReading();
        User currentUsername = new User("testuser", "testpassword");
        currentUsername.setLastReadingDate(new java.util.Date());

        // Создание объекта ByteArrayOutputStream для захвата вывода
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        // Замена System.out на созданный PrintStream
        PrintStream originalOut = System.out;
        System.setOut(printStream);

        // Подготовка тестовой базы данных в контейнере
        try (Connection connection = DriverManager.getConnection(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {
            String query = "CREATE TABLE users (username VARCHAR(255), last_reading_date DATE)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Создание mock объекта для класса Admin
        Admin adminMock = mock(Admin.class);

        // Вызов метода logout
        main.logout(currentUserReading, currentUsername, adminMock);

        // Проверка, что вывод содержит сообщение о выходе
        assertTrue(outputStream.toString().contains("Logging out..."));

        // Восстановление System.out
        System.setOut(originalOut);
    }
}
