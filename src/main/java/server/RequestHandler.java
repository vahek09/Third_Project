package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RequestHandler {
    private static final Gson gson = new Gson();
    private DatabaseManager databaseManager;

    public RequestHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public RequestHandler() {
    }

    public String processCommand(JsonObject request) {
        String type = request.get("type").getAsString();
        JsonArray keyArray = request.has("key") ? request.getAsJsonArray("key") : new JsonArray();
        JsonElement value = request.has("value") ? request.get("value") : null;
        JsonObject response = new JsonObject();
        response.addProperty("response", Constants.RESPONSE_ERROR);

        switch (type) {
            case "set":
                if (!keyArray.isEmpty() && value != null) {
                    String[] keyPath = jsonArrayToStringArray(keyArray);
                    if (databaseManager.set(keyPath, value)) {
                        response.addProperty("response", Constants.RESPONSE_OK);
                    } else {
                        response.addProperty("reason", Constants.REASON_FAILED_TO_SET);
                    }
                    System.out.println("Request type: " + type);
                    System.out.println("Key array: " + keyArray.toString());
                    System.out.println("Value: " + value);
                    System.out.println("DatabaseManager set return: " + databaseManager.set(keyPath, value));

                } else {
                    response.addProperty("reason", Constants.REASON_MISSING_KEY_OR_VALUE);
                }

                break;
            case "get":
                if (!keyArray.isEmpty()) {
                    String[] keyPath = jsonArrayToStringArray(keyArray);
                    JsonElement getValue = databaseManager.get(stringArrayToJsonArray(keyPath));
                    if (getValue != null && !getValue.isJsonNull()) {
                        response.addProperty("response", Constants.RESPONSE_OK);
                        response.add("value", getValue);
                    } else {
                        response.addProperty("response", Constants.RESPONSE_ERROR);
                        response.addProperty("reason", Constants.REASON_NO_SUCH_KEY);
                    }
                } else {
                    response.addProperty("response", Constants.RESPONSE_ERROR);
                    response.addProperty("reason", Constants.REASON_MISSING_KEY);
                }
                break;
            case "delete":
                if (!keyArray.isEmpty()) {
                    String[] keyPath = jsonArrayToStringArray(keyArray);
                    if (databaseManager.delete(stringArrayToJsonArray(keyPath))) {
                        response.addProperty("response", Constants.RESPONSE_OK);
                    } else {
                        response.addProperty("reason", Constants.REASON_NO_SUCH_KEY);
                    }
                } else {
                    response.addProperty("reason", Constants.REASON_MISSING_KEY);
                }
                break;
            case "exit":
                response.addProperty("response", Constants.RESPONSE_OK);
                break;
            default:
                response.addProperty("response", Constants.RESPONSE_ERROR);
                response.addProperty("reason", Constants.REASON_INVALID_COMMAND);
        }

        return gson.toJson(response);
    }

    String[] jsonArrayToStringArray(JsonArray jsonArray) {
        String[] result = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.get(i).getAsString();
        }
        return result;
    }

    JsonArray stringArrayToJsonArray(String[] keyPath) {
        JsonArray jsonArray = new JsonArray();
        for (String key : keyPath) {
            jsonArray.add(key);
        }
        return jsonArray;
    }

    protected DatabaseManager createDatabaseManager() {
        return new DatabaseManager();
    }
}
