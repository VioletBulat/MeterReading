package Counter;

import java.text.SimpleDateFormat;
import java.util.*;

public class MeterReading {

    // класс для сохранения показаний

    private int coldWaterReading;
    private int hotWaterReading;
    private final Map<String, List<Integer>> monthlyData;
    private final Map<String, List<Integer>> historyData;

    public MeterReading() {
        this.monthlyData = new HashMap<>();
        this.historyData = new TreeMap<>();
    }

    public int getColdWaterReading() { return coldWaterReading; }
    public int getHotWaterReading() {
        return hotWaterReading;
    }

    // метод для ввода данных
    public void enterReadings() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter cold water readings: ");
        int coldWaterReading = scanner.nextInt();

        System.out.println("Enter hot water readings: ");
        int hotWaterReading = scanner.nextInt();

        this.coldWaterReading = coldWaterReading;
        this.hotWaterReading = hotWaterReading;

        // сохраняем текущие показания в месяцах
        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.getTime());

        monthlyData.computeIfAbsent(currentMonth, k -> new ArrayList<>());
        monthlyData.get(currentMonth).add(coldWaterReading);
        monthlyData.get(currentMonth).add(hotWaterReading);

        // сохраняем текущие показания в истории
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
        historyData.computeIfAbsent(currentDate, k -> new ArrayList<>());
        historyData.get(currentDate).add(coldWaterReading);
        historyData.get(currentDate).add(hotWaterReading);

        System.out.println("Readings entered successfully!");
    }

    // выводим текущие данные
    public void showCurrentData() {
        System.out.println("Current readings: ");
        System.out.println("Cold water: " + coldWaterReading);
        System.out.println("Hot water: " + hotWaterReading);
    }

    // выводим данные за месяц
    public void showMonthlyData(String month) {
            List<Integer> readings = monthlyData.get(month);
            System.out.println(readings);
            if (readings != null && !readings.isEmpty()) {
                System.out.println("Monthly readings for " + month + ":");
                for (int i = 0; i < readings.size(); i += 2) {
                    System.out.println("Cold water: " + readings.get(i));
                    System.out.println("Hot water: " + readings.get(i + 1));
                    System.out.println("---");
                }
            } else {
                System.out.println("No readings available for the specified month.");
            }
        }

    // выводим историю данных
    public void showDataHistory() {
        if (!historyData.isEmpty()) {
            System.out.println("Data history: ");
            for (Map.Entry<String, List<Integer>> entry : historyData.entrySet()) {
                String date = entry.getKey();
                List<Integer> readings = entry.getValue();
                System.out.println("Date: " + date);
                System.out.println("Cold water: " + readings.get(0));
                System.out.println("Hot water: " + readings.get(1));
                System.out.println("---");
            }
        } else {
            System.out.println("No data history available.");
        }
    }
}
