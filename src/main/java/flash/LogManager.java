package flash;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class LogManager {
    private final List<String> log = new ArrayList<>();

    public void logAndPrint(String message) {
        System.out.println(message);
        log.add(message);
    }

    public void addToLog(String message) {
        log.add(message);
    }

    public void saveLog(Scanner scanner) {
        logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        addToLog(fileName);
        logAndPrint("The log has been saved.");
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (String entry : log) {
                writer.println(entry);
            }
            writer.println("Log saved on: " + new Date());
        } catch (IOException e) {
            logAndPrint("Error writing to the file: " + e.getMessage());
        }
    }
}
