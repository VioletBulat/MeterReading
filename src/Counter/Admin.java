package Counter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Admin {

    // класс админа

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final List<String> logs = new ArrayList<>();

    public static void logOperations(String operation) {
        Date date = new Date();
        String logEntry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + " - " + operation;
        logs.add(logEntry);
    }

    public static void showLogs() {
            System.out.println("Admin logs: ");
            for (String logEntry : logs) {
                System.out.println(logEntry);
            }
    }
}
