package server;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseManager implements DatabaseManagerInterface{
    private static final Gson gson = new Gson();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final String PATH = System.getProperty("user.dir") + "/src/main/java/server/data/db.json";

    public boolean set(String[] keyPath, JsonElement value) {
        writeLock.lock();
        try {
            JsonObject database = loadDatabase();
            if (database == null) {
                database = new JsonObject();  // Initialize if the database is null
            }

            JsonObject current = database;
            for (int i = 0; i < keyPath.length; i++) {
                String key = keyPath[i];
                boolean isLastKey = i == keyPath.length - 1;

                if (current.has(key)) {
                    JsonElement nextElement = current.get(key);
                    if (nextElement.isJsonObject()) {
                        current = nextElement.getAsJsonObject();
                    } else if (isLastKey) {
                        current.add(key, value);
                    } else {
                        return false;  // Next element is not a JsonObject, and we're not at the last key
                    }
                } else {
                    if (isLastKey) {
                        current.add(key, value);  // Add the value if it's the last key
                    } else {
                        JsonObject newObject = new JsonObject();
                        current.add(key, newObject);  // Create a new object for the next level
                        current = newObject;
                    }
                }
            }

            saveDatabase(database);  // Save the updated database
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public JsonElement get(JsonArray keyPath) {
        readLock.lock();
        try {
            JsonElement current = loadDatabase();

            if (current == null) {
                return JsonNull.INSTANCE;
            }

            for (int i = 0; i < keyPath.size(); i++) {
                String key = keyPath.get(i).getAsString();
                if (current.isJsonObject() && current.getAsJsonObject().has(key)) {
                    current = current.getAsJsonObject().get(key);
                } else {
                    return JsonNull.INSTANCE;
                }
            }
            return current;
        } catch (IOException e) {
            e.printStackTrace();
            return JsonNull.INSTANCE;
        } finally {
            readLock.unlock();
        }
    }

    public boolean delete(JsonArray keyPath) {
        writeLock.lock();
        try {
            JsonObject database = loadDatabase();
            if (database == null) {
                return false;
            }

            JsonElement current = database;

            // Traverse to the parent of the key to be deleted
            for (int i = 0; i < keyPath.size() - 1; i++) {
                String key = keyPath.get(i).getAsString();
                if (current.isJsonObject() && current.getAsJsonObject().has(key)) {
                    current = current.getAsJsonObject().get(key);
                } else {
                    return false;
                }
            }

            // Get the last key in the path
            String lastKey = keyPath.get(keyPath.size() - 1).getAsString();

            // Ensure the key exists and remove it
            if (current.isJsonObject()) {
                JsonObject parentObject = current.getAsJsonObject();
                if (parentObject.has(lastKey)) {
                    parentObject.remove(lastKey); // Remove the key
                    saveDatabase(database); // Save the database after removing the key
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            writeLock.unlock();
        }
    }


    private JsonObject loadDatabase() throws IOException {
        File file = new File(PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, JsonObject.class);
            }
        }
        return null;
    }

    void saveDatabase(JsonObject database) throws IOException {
        try (FileWriter writer = new FileWriter(PATH)) {
            gson.toJson(database, writer);
        }
    }
}
