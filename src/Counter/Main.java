package Counter;

import java.util.*;

public class Main {

    // основной класс

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

    // метод для авторизации пользователей
    private static void login() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();

            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            // проверка на корректность введенных данных

            if (users.containsKey(username) && users.get(username).equals(password)) {
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

        // проверяем существует ли такой логин
        if(users.containsKey(userName)) {
            System.out.println("Username already exists. Please choose a different one.");
            register(); // рекурсия
            return;
        }

        System.out.println("Enter a new password: ");
        String password = scanner.nextLine();

        users.put(userName, password);
        System.out.println("Registration successful! Now you can log in.");
        currentUsername = new User(userName, password);

        Admin.logOperations(currentUsername.getUsername() + " registered.");
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

    // метод для внесения показаний пользователями
    public static void dataInput() {
        if(currentUserReading == null) {
            currentUserReading = new MeterReading();
        }
        if (currentUsername.getLastReadingDate() == null || isMonthPassed(currentUsername.getLastReadingDate())) {
            User currentUser = currentUsername;
            currentUserReading.enterReadings();
            Admin.logOperations(currentUser.getUsername() + " entered readings.");
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
            currentUserReading.showCurrentData();
            Admin.logOperations(currentUsername.getUsername() + " viewed the current readings.");
        } else {
            System.out.println("No readings available. Please input readings first.");
        }
    }

    // метод для вывода данных за месяц
    public static void monthlyData() {
        if (currentUserReading != null) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter month to view data: ");
            String month = scanner.nextLine();
            currentUserReading.showMonthlyData(month);
            Admin.logOperations(currentUsername.getUsername() + " viewed the readings for the month.");
        } else {
            System.out.println("No readings available. Please input readings first.");
        }
    }

    // метод для вывода истории данных
    public static void dataHistory() {
        if(currentUserReading != null) {
            currentUserReading.showDataHistory();
            Admin.logOperations(currentUsername.getUsername() + " viewed the reading history.");
        } else {
            System.out.println("No readings available. Please input readings first.");
        }
    }

    // Выход из системы
    public static void logout() {
        if(currentUserReading != null) {
            System.out.println("Logging out...");
            Admin.logOperations(currentUsername.getUsername() + " logged out.");
            currentUserReading = null;
        } else {
            System.out.println("No user logged in.");
        }
    }

    // логирование действий пользователей
    public static void showAdminLogs() {
        Admin.showLogs();
    }
}
