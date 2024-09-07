public class Constants {
    // JSON Keys
    public static final String KEY_TYPE = "type";
    public static final String KEY_KEY = "key";
    public static final String KEY_VALUE = "value";

    // Command Types
    public static final String TYPE_SET = "set";
    public static final String TYPE_GET = "get";
    public static final String TYPE_DELETE = "delete";
    public static final String TYPE_EXIT = "exit";
    public static final String TYPE_INVALID = "invalid";

    // JSON Responses
    public static final String RESPONSE_OK = "{\"response\":\"OK\"}";
    public static final String RESPONSE_ERROR = "{\"response\":\"ERROR\"}";

    // Error Reasons
    public static final String REASON_INVALID_COMMAND = "Invalid command";
    public static final String REASON_NO_SUCH_KEY = "No such key";
    public static final String REASON_MISSING_KEY_OR_VALUE = "Missing key or value";
    public static final String REASON_MISSING_KEY = "Missing key";

    // Other Constants
    public static final String LARGE_VALUE = "x".repeat(10000); // Large value
}
