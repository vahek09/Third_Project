package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.Constants;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ClientMainTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("testFile", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Test content");
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    @DisplayName("Build message for 'set' command")
    void testBuildMessageForSet() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_SET);
        client.cmdArgs.setKey("testKey");
        client.cmdArgs.setValue("testValue");

        String expectedJson = String.format("{\"%s\":\"%s\",\"%s\":\"testKey\",\"%s\":\"testValue\"}",
                Constants.KEY_TYPE, Constants.TYPE_SET,
                Constants.KEY_KEY, "value",
                Constants.KEY_VALUE, "value");
        assertEquals(expectedJson, client.buildMessage());
    }

    @Test
    @DisplayName("Build message for 'get' command")
    void testBuildMessageForGet() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_GET);
        client.cmdArgs.setKey("testKey");

        String expectedJson = String.format("{\"%s\":\"%s\",\"%s\":\"testKey\"}",
                Constants.KEY_TYPE, Constants.TYPE_GET,
                Constants.KEY_KEY, "testKey");
        assertEquals(expectedJson, client.buildMessage());
    }

    @Test
    @DisplayName("Build message for 'delete' command")
    void testBuildMessageForDelete() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_DELETE);
        client.cmdArgs.setKey("testKey");

        String expectedJson = String.format("{\"%s\":\"%s\",\"%s\":\"testKey\"}",
                Constants.KEY_TYPE, Constants.TYPE_DELETE,
                Constants.KEY_KEY, "testKey");
        assertEquals(expectedJson, client.buildMessage());
    }

    @Test
    @DisplayName("Build message for 'set' command and parse JSON")
    void testSetCommand() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_SET);
        client.cmdArgs.setKey("testKey");
        client.cmdArgs.setValue("testValue");
        String message = client.buildMessage();
        JsonObject request = JsonParser.parseString(message).getAsJsonObject();

        assertEquals(Constants.TYPE_SET, request.get(Constants.KEY_TYPE).getAsString());
        assertEquals("testKey", request.get(Constants.KEY_KEY).getAsString());
        assertEquals("testValue", request.get(Constants.KEY_VALUE).getAsString());
    }

    @Test
    @DisplayName("Build message for 'get' command and parse JSON")
    void testGetCommand() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_GET);
        client.cmdArgs.setKey("testKey");
        String message = client.buildMessage();
        JsonObject request = JsonParser.parseString(message).getAsJsonObject();

        assertEquals(Constants.TYPE_GET, request.get(Constants.KEY_TYPE).getAsString());
        assertEquals("testKey", request.get(Constants.KEY_KEY).getAsString());
    }

    @Test
    @DisplayName("Build message for 'delete' command and parse JSON")
    void testDeleteCommand() {
        Main client = new Main();
        client.cmdArgs.setType(Constants.TYPE_DELETE);
        client.cmdArgs.setKey("testKey");
        String message = client.buildMessage();

        // Print the actual JSON message for debugging
        System.out.println("Generated JSON: " + message);

        JsonObject request = JsonParser.parseString(message).getAsJsonObject();

        assertTrue(request.has(Constants.KEY_TYPE), "Expected JSON to contain type key");
        assertTrue(request.has(Constants.KEY_KEY), "Expected JSON to contain key key");

        assertEquals(Constants.TYPE_DELETE, request.get(Constants.KEY_TYPE).getAsString());
        assertEquals("testKey", request.get(Constants.KEY_KEY).getAsString());
    }


    @Test
    @DisplayName("Run client with input file")
    void testRunWithInputFile() throws IOException {
        Main client = spy(new Main());
        client.cmdArgs.setInputFile("client/data/testFile.txt");

        File tempFile = File.createTempFile("testFile", ".txt");
        Files.write(tempFile.toPath(), "testMessage".getBytes());

        client.cmdArgs.setInputFile(tempFile.getName());

        doNothing().when(client).handleFileInput(anyString(), anyInt(), anyString());
        doNothing().when(client).handleSingleRequest(anyString(), anyInt());
        client.run();

        verify(client, times(1)).handleFileInput(anyString(), anyInt(), anyString());
        verify(client, never()).handleSingleRequest(anyString(), anyInt());
    }

    @Test
    @DisplayName("Run client without input file")
    void testRunWithoutInputFile() {
        Main client = spy(new Main());
        client.cmdArgs.setInputFile(null);

        doNothing().when(client).handleFileInput(anyString(), anyInt(), anyString());
        doNothing().when(client).handleSingleRequest(anyString(), anyInt());
        client.run();

        verify(client, never()).handleFileInput(anyString(), anyInt(), anyString());
        verify(client, times(1)).handleSingleRequest(anyString(), anyInt());
    }

    @Test
    @DisplayName("Main method with invalid command-line arguments")
    void testMainWithInvalidArgs() {
        String[] args = {"--invalid-arg"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Main.main(args);

            String output = outputStream.toString();

            assertTrue(output.contains("Error parsing command-line arguments:"), "Expected output to contain error message");

        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Handle file input and verify sendRequest calls")
    void testHandleFileInput() throws IOException {
        Main client = spy(new Main());
        String address = "localhost";
        int port = 34567;

        doNothing().when(client).sendRequest(anyString(), anyInt(), anyString());

        File testFile = new File("src/test/resources/testFile.txt");
        if (!testFile.exists()) {
            testFile.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
                writer.write("message1\n");
                writer.write("message2\n");
            }
        }

        client.handleFileInput(address, port, testFile.getName());

        verify(client, times(1)).sendRequest(eq(address), eq(port), eq("message1"));
        verify(client, times(1)).sendRequest(eq(address), eq(port), eq("message2"));
    }

    @Test
    @DisplayName("Handle single request and verify sendRequest call")
    void testHandleSingleRequest() {
        // Given
        Main client = spy(new Main());
        String address = "localhost";
        int port = 34567;
        String expectedMessage = String.format("{\"%s\":\"test\",\"%s\":\"testKey\"}",
                Constants.KEY_TYPE, "test",
                Constants.KEY_KEY, "testKey");

        doReturn(expectedMessage).when(client).buildMessage();
        doNothing().when(client).sendRequest(anyString(), anyInt(), anyString());

        // When
        client.handleSingleRequest(address, port);

        // Then
        verify(client, times(1)).buildMessage();
        verify(client, times(1)).sendRequest(eq(address), eq(port), eq(expectedMessage));
    }
}
