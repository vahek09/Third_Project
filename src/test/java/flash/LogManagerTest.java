package flash;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;


class LogManagerTest {

    private LogManager logManager;

    @BeforeEach
    void setUp() {
        logManager = new LogManager();
    }

    @Test
    @DisplayName("Test addToLog and logAndPrint functionalities")
    void testLogManagerMethods() {
        LogManager spyLogManager = spy(logManager);
        //given
        String message = "Test message";

        //when
        spyLogManager.addToLog(message);
        spyLogManager.logAndPrint(message);

        //then
        assertTrue(spyLogManager.getLog().contains(message), "Log should contain the message.");
        verify(spyLogManager).logAndPrint(message);
        verify(spyLogManager).addToLog(message);
    }

    @Test
    @DisplayName("Save Log with temp file")
    void testSaveLogWithTempFile() throws IOException {
        //given
        Path tempFilePath = Files.createTempFile("testLog", ".txt");
        ByteArrayInputStream simulatedInput = new ByteArrayInputStream((tempFilePath.toString() + "\n").getBytes());
        System.setIn(simulatedInput);

        Scanner scanner = new Scanner(System.in);

        logManager.logAndPrint("First log entry");
        logManager.logAndPrint("Second log entry");

        // when
        logManager.saveLog(scanner);

        // then
        List<String> fileContent = Files.readAllLines(tempFilePath);
        assertTrue(fileContent.contains("First log entry"));
        assertTrue(fileContent.contains("Second log entry"));
        assertTrue(fileContent.get(fileContent.size() - 1).startsWith("Log saved on:"));

        Files.deleteIfExists(tempFilePath);
        System.setIn(System.in);
    }
}