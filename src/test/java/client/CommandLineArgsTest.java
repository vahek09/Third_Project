package client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.Constants;
import static org.junit.jupiter.api.Assertions.*;

class CommandLineArgsTest {

    @Test
    @DisplayName("Should correctly set and get 'set' command parameters")
    void testCommandLineArgsSet() {
        CommandLineArgs args = new CommandLineArgs();
        args.setType(Constants.TYPE_SET);
        args.setKey("testKey");
        args.setValue("testValue");

        assertEquals(Constants.TYPE_SET, args.getType());
        assertEquals("testKey", args.getKey());
        assertEquals("testValue", args.getValue());
    }

    @Test
    @DisplayName("Should correctly set and get 'get' command parameters")
    void testCommandLineArgsGet() {
        CommandLineArgs args = new CommandLineArgs();
        args.setType(Constants.TYPE_GET);
        args.setKey("testKey");

        assertEquals(Constants.TYPE_GET, args.getType());
        assertEquals("testKey", args.getKey());
        assertNull(args.getValue());
    }
}
