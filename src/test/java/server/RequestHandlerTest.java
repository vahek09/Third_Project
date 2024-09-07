package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestHandlerTest {

    private DatabaseManager mockDatabaseManager;
    private RequestHandler requestHandler;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        mockDatabaseManager = mock(DatabaseManager.class);
        requestHandler = new RequestHandler(mockDatabaseManager);
        gson = new Gson();
    }

    @Test
    @DisplayName("Test Set Command Success")
    public void testSetCommandSuccess() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "set");
        JsonArray keyArray = new JsonArray();
        keyArray.add("test");
        request.add("key", keyArray);
        request.addProperty("value", "testValue");

        when(mockDatabaseManager.set(any(String[].class), any())).thenReturn(true);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_OK, jsonResponse.get("response").getAsString());
        assertFalse(jsonResponse.has("reason"));
    }

    @Test
    @DisplayName("Test Set Command Failure")
    public void testSetCommandFailure() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "set");
        JsonArray keyArray = new JsonArray();
        keyArray.add("test");
        request.add("key", keyArray);
        request.addProperty("value", "testValue");

        when(mockDatabaseManager.set(any(String[].class), any())).thenReturn(false);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_FAILED_TO_SET, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Set Command Missing Key or Value")
    public void testSetCommandMissingKeyOrValue() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "set");

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_MISSING_KEY_OR_VALUE, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Get Command Success")
    public void testGetCommandSuccess() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "get");
        JsonArray keyArray = new JsonArray();
        keyArray.add("test");
        request.add("key", keyArray);

        JsonPrimitive mockValue = new JsonPrimitive("testValue");
        when(mockDatabaseManager.get(any(JsonArray.class))).thenReturn(mockValue);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_OK, jsonResponse.get("response").getAsString());
        assertEquals("testValue", jsonResponse.get("value").getAsString());
    }

    @Test
    @DisplayName("Test Get Command No Such Key")
    public void testGetCommandNoSuchKey() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "get");
        JsonArray keyArray = new JsonArray();
        keyArray.add("test");
        request.add("key", keyArray);

        when(mockDatabaseManager.get(any(JsonArray.class))).thenReturn(null);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_NO_SUCH_KEY, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Get Command Missing Key")
    public void testGetCommandMissingKey() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "get");

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_MISSING_KEY, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Delete Command Success")
    public void testDeleteCommandSuccess() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "delete");
        JsonArray keyArray = new JsonArray();
        keyArray.add("test");
        request.add("key", keyArray);

        when(mockDatabaseManager.delete(any(JsonArray.class))).thenReturn(true);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_OK, jsonResponse.get("response").getAsString());
    }

    @Test
    @DisplayName("Test Delete Command No Such Key")
    public void testDeleteCommandNoSuchKey() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "delete");
        JsonArray keyArray = new JsonArray();
        keyArray.add("nonexistent");
        request.add("key", keyArray);

        when(mockDatabaseManager.delete(any(JsonArray.class))).thenReturn(false);

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_NO_SUCH_KEY, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Delete Command Missing Key")
    public void testDeleteCommandMissingKey() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "delete");

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_MISSING_KEY, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test Exit Command")
    public void testExitCommand() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "exit");

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_OK, jsonResponse.get("response").getAsString());
    }

    @Test
    @DisplayName("Test Invalid Command")
    public void testInvalidCommand() {
        JsonObject request = new JsonObject();
        request.addProperty("type", "invalidCommand");

        String response = requestHandler.processCommand(request);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals(Constants.RESPONSE_ERROR, jsonResponse.get("response").getAsString());
        assertEquals(Constants.REASON_INVALID_COMMAND, jsonResponse.get("reason").getAsString());
    }

    @Test
    @DisplayName("Test JSON Array to String Array Conversion")
    public void testJsonArrayToStringArray() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("test1");
        jsonArray.add("test2");

        String[] result = requestHandler.jsonArrayToStringArray(jsonArray);

        assertArrayEquals(new String[]{"test1", "test2"}, result);
    }

    @Test
    @DisplayName("Test String Array to JSON Array Conversion")
    public void testStringArrayToJsonArray() {
        String[] stringArray = {"test1", "test2"};

        JsonArray result = requestHandler.stringArrayToJsonArray(stringArray);

        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).getAsString());
        assertEquals("test2", result.get(1).getAsString());
    }
}
