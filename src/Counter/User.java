package Counter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {

    // класс с информацией о пользователях

    private final String username;
    private final String password;
    private final MeterReading meterReading;
    private final List<String> logs;
    private Date lastReadingDate;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.meterReading = new MeterReading();
        this.logs = new ArrayList<>();
    }

    public String getPassword() {
        return password;
    }
    public String getUsername() { return username; }
    public MeterReading getMeterReading() { return meterReading; }
    public Date getLastReadingDate() { return lastReadingDate; }
    public void setLastReadingDate(Date lastReadingDate) {
        this.lastReadingDate =  lastReadingDate;
    }

    public void showLogs() {
        System.out.println("User logs: ");
        for(String logEntry : logs) {
            System.out.println(logEntry);
        }
    }
}
