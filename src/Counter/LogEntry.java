package Counter;

public class LogEntry {

    // логирование

    private final String username;
    private final String entry;

    public LogEntry(String username, String entry) {
        this.username = username;
        this.entry = entry;
    }

    public String getEntry() {
        return entry;
    }
}
