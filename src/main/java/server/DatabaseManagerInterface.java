package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public interface DatabaseManagerInterface {
    boolean set(String[] keyPath, JsonElement value);
    JsonElement get(JsonArray keyPath);
    boolean delete(JsonArray keyPath);
}
