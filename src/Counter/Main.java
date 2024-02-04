package Counter;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Scanner;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/ylab";
    private static final String USER_NAME = "ylab";
    private static final String PASSWORD = "ylab";

    // список для хранения пользователей (логин, пароль)
    private static final Map<String, String> users = new HashMap<>();
    // добавляем админа
    static { users.put("admin", "admin"); }

    // текущие показания пользователей
    private static MeterReading currentUserReading;
    // текущие пользователи
    private static User currentUsername;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1.Login");
            System.out.println("2.Registration");
            System.out.println("3.Exit");
            System.out.println("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    login();
                    showOptions();
                    break;
                case 2:
                    register();
                    showOptions();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // проверка пользователя в базе данных
    private static boolean validateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // метод для авторизации пользователей
    private static void login() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();

            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            // проверка на корректность введенных данных
            if(validateUser(username, password)) {
                currentUsername = new User(username, password);
                currentUserReading = new MeterReading();
                System.out.println("Login successful!");
                Admin.logOperations(currentUsername.getUsername() + " logged in.");
                break;
            } else {
                System.out.println("Login failed. Would you like to register? (y/n)");
                String choice = scanner.nextLine().toLowerCase();

                if ("y".equals(choice)) {
                    register();
                    break;
                } else {
                    System.out.println("Try again.");
                }
            }
        }
    }

    // метод для регистрации
    private static void register() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a new username: ");
        String userName = scanner.nextLine();

        if(userExists(userName)) {
            System.out.println("Username already exists. Please choose a different one.");
            register();
            return;
        }

        System.out.println("Enter a new password: ");
        String password = scanner.nextLine();

        saveUser(userName, password);
        //users.put(userName, password);
        System.out.println("Registration successful! Now you can log in.");
        currentUsername = new User(userName, password);

        Admin.logOperations(currentUsername.getUsername() + " registered.");
    }

    // проверка на существование пользователя в базе данных
    private static boolean userExists(String username) {
        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
            String query = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // сохранение пользователя в базе данных
    private static void saveUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)){
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // показывает доступные опции после авторизации
    private static void showOptions() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("Select an action: ");
            System.out.println("1. Data input");
            System.out.println("2. Current data");
            System.out.println("3. Monthly data");
            System.out.println("4. Data history");
            System.out.println("5. Show admin logs");
            System.out.println("6. Logout");

            if(scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid choice. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    dataInput();
                    break;
                case 2:
                    currentData();
                    break;
                case 3:
                    monthlyData();
                    break;
                case 4:
                    dataHistory();
                    break;
                case 5:
                    if("admin".equals(currentUsername.getUsername())) {
                        showAdminLogs();
                    } else {
                        System.out.println("Permission denied. Only Admin can view logs.");
                    }
                    break;
                case 6:
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 6);
    }

    // метод для внесения показаний пользователями в базу данных
    public static void dataInput() {
        if (currentUserReading == null) {
            currentUserReading = new MeterReading();
        }
        if (currentUsername.getLastReadingDate() == null || isMonthPassed(currentUsername.getLastReadingDate())) {
            User currentUser = currentUsername;
            currentUserReading.enterReadings();

            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
                String query = "INSERT INTO meter_readings (username, cold_water_reading, hot_water_reading, reading_date) " +
                        "VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                    preparedStatement.setString(1, currentUser.getUsername());
                    preparedStatement.setInt(2, currentUserReading.getColdWaterReading());
                    preparedStatement.setInt(3, currentUserReading.getHotWaterReading());
                    preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Admin.logOperations(currentUser.getUsername());
            currentUser.setLastReadingDate(new Date());
        } else {
            System.out.println("You can only input data once a month. Try again later.");
        }
    }

    // проверка прошел ли месяц с момента подачи последних показаний
    private static boolean isMonthPassed(Date lastReadingDate) {
        Calendar currentDate = Calendar.getInstance();
        Calendar lastDate = Calendar.getInstance();
        lastDate.setTime(lastReadingDate);

        int currentYear = currentDate.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH);

        int lastYear = lastDate.get(Calendar.YEAR);
        int lastMonth = lastDate.get(Calendar.MONTH);

        return currentYear > lastYear || (currentYear == lastYear && currentMonth > lastMonth);
    }

    // метод для вывода актуальных данных
    public static void currentData() {
        if (currentUserReading != null) {
            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
                String query = "SELECT cold_water_reading, hot_water_reading FROM meter_reading WHERE username = ? " +
                        "ORDER BY entry_date DESC LIMIT 1";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, currentUsername.getUsername());

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if(resultSet.next()) {
                            int coldWaterReading = resultSet.getInt("cold_water_reading");
                            int hotWaterReading = resultSet.getInt("hot_water_reading");

                            System.out.println("Current readings:");
                            System.out.println("Cold water: " + coldWaterReading);
                            System.out.println("Hot water: " + hotWaterReading);
                        } else {
                            System.out.println("No readings available. Please input reading first.");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No readings available. Please input reading first.");
        }
    }

    // метод для вывода данных за месяц
    public static void monthlyData() {
        if (currentUserReading != null) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter month to view data: ");
            String month = scanner.nextLine();

            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
                String query = "SELECT cold_water_reading, hot_water_reading, entry_date FROM meter_reading " +
                        "WHERE username = ? AND EXTRACT(MONTH FROM entry_date) = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, currentUsername.getUsername());
                    preparedStatement.setString(2, month);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        boolean dataFound = false;

                        while (resultSet.next()) {
                            int coldWaterReading = resultSet.getInt("cold_water_reading");
                            int hotWaterReading = resultSet.getInt("hot_water_reading");
                            String entryDate = resultSet.getString("entry_date");

                            System.out.println("Readings for " + entryDate + ":");
                            System.out.println("Cold water: " + coldWaterReading);
                            System.out.println("Hot water: " + hotWaterReading);
                            System.out.println("---");

                            dataFound = true;
                        }

                        if (!dataFound) {
                            System.out.println("No readings available for the specified month.");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No readings available for the specified month.");
        }
    }

    // метод для вывода истории данных
    public static void dataHistory() {
        if(currentUserReading != null) {
            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
                String query = "SELECT cold_water_reading, hot_water_reading, entry_date FROM meter_reading " +
                        "WHERE username = ? ORDER BY entry_date DESC";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, currentUsername.getUsername());

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        boolean dataFound = false;

                        while (resultSet.next()) {
                            int coldWaterReading = resultSet.getInt("cold_water_reading");
                            int hotWaterReading = resultSet.getInt("hot_water_reading");
                            String entryDate = resultSet.getString("entry_date");

                            System.out.println("Readings for " + entryDate + ":");
                            System.out.println("Cold water: " + coldWaterReading);
                            System.out.println("Hot water: " + hotWaterReading);
                            System.out.println("---");

                            dataFound = true;
                        }

                        if (!dataFound) {
                            System.out.println("No data history available.");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No readings available. Please input readings first.");
        }
    }

    // Выход из системы
    public static void logout() {
        if(currentUserReading != null) {
            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)) {
                String query = "UPDATE users SET last_reading_date = ? WHERE username = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setDate(1, new java.sql.Date(new Date().getTime()));
                    preparedStatement.setString(2, currentUsername.getUsername());
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Logging out...");
            Admin.logOperations(currentUsername.getUsername() + " logged out.");
            currentUserReading = null;
        } else {
            System.out.println("No user logged in.");
        }
    }

    // логирование действий пользователей
    private static void showAdminLogs() {
        if("admin".equals(currentUsername.getUsername())) {
            try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)){
                String sql = "SELECT * FROM, admin_logs";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()){
                        while (resultSet.next()) {
                            System.out.println(resultSet.getString("log_entry"));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Permission denied. Only Admin can view logs.");
        }
    }
}
