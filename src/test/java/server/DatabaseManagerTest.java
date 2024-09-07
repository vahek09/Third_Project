package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static server.ConnectionManager.gson;

class DatabaseManagerTest {
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws IOException {
        databaseManager = new DatabaseManager();
        File dbFile = new File(System.getProperty("user.dir") + "/src/main/java/server/data/db.json");
        if (dbFile.exists()) {
            try (FileWriter writer = new FileWriter(dbFile)) {
                writer.write("{}");
            }
        }
    }

    @Test
    @DisplayName("Test Set Success")
    void testSetSuccess() {
        String[] keyPath = {"key1", "key2"};
        JsonElement value = new JsonPrimitive("testValue");
        assertTrue(databaseManager.set(keyPath, value));
    }

    @Test
    @DisplayName("Test Get Success")
    void testGetSuccess() {
        String[] keyPath = {"key1", "key2"};
        JsonElement value = new JsonPrimitive("testValue");
        databaseManager.set(keyPath, value);

        JsonArray keyPathArray = new JsonArray();
        keyPathArray.add("key1");
        keyPathArray.add("key2");

        JsonElement result = databaseManager.get(keyPathArray);
        assertNotNull(result);
        assertEquals(value, result);
    }

    @Test
    @DisplayName("Test Get Non-Existent Key")
    void testGetNonExistentKey() {
        JsonArray keyPathArray = new JsonArray();
        keyPathArray.add("nonExistentKey");
        JsonElement result = databaseManager.get(keyPathArray);
        assertTrue(result.isJsonNull());
    }

    @Test
    @DisplayName("Test Delete Success")
    void testDeleteSuccess() throws IOException {
        JsonElement value = new JsonPrimitive("testValue");
        String[] keyPath = {"key1", "key2"};
        JsonArray keyPathArray = new JsonArray();
        keyPathArray.add("key1");
        keyPathArray.add("key2");

        databaseManager.set(keyPath, value);

        JsonElement result = databaseManager.get(keyPathArray);
        assertNotNull(result);

        assertTrue(databaseManager.delete(keyPathArray));

        JsonObject db = loadDatabase();
        assertNotNull(db);
        assertNull(db.get("key1").getAsJsonObject().get("key2"), "key2 should not exist in the database");
    }

    @Test
    @DisplayName("Test Delete Non-Existent Key")
    void testDeleteNonExistentKey() {
        JsonArray keyPathArray = new JsonArray();
        keyPathArray.add("nonExistentKey");
        assertFalse(databaseManager.delete(keyPathArray));
    }

    private JsonObject loadDatabase() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/src/main/java/server/data/db.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, JsonObject.class);
            }
        }
        return null;
    }
}
