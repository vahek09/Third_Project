package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionManagerTest {

    private ConnectionManager connectionManager;
    private ExecutorService mockExecutor;
    private ServerSocket mockServerSocket;
    private RequestHandler mockRequestHandler;

    @BeforeEach
    void setUp() {
        mockExecutor = mock(ExecutorService.class);
        mockServerSocket = mock(ServerSocket.class);
        mockRequestHandler = mock(RequestHandler.class);

        connectionManager = new ConnectionManager();
        connectionManager.setExecutor(mockExecutor);
        connectionManager.setServerSocket(mockServerSocket);
    }

    @Test
    @DisplayName("Test Set Executor")
    void testSetExecutor() {
        ExecutorService newExecutor = Executors.newFixedThreadPool(5);
        connectionManager.setExecutor(newExecutor);
        assertNotNull(connectionManager);
    }

    @Test
    @DisplayName("Test Set Server Socket")
    void testSetServerSocket() {
        ServerSocket newServerSocket = mock(ServerSocket.class);
        connectionManager.setServerSocket(newServerSocket);
        assertNotNull(connectionManager);
    }

    @Test
    @DisplayName("Test Shutdown Server")
    void testShutdownServer() throws IOException, InterruptedException {
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS)).thenReturn(true);
        connectionManager.shutdownServer();
        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(60, TimeUnit.SECONDS);
        verify(mockServerSocket).close();
    }

    @Test
    @DisplayName("Test Shutdown Server with Forceful Shutdown")
    void testShutdownServerWithForcefulShutdown() throws IOException, InterruptedException {
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS))
                .thenReturn(false)
                .thenReturn(true);
        connectionManager.shutdownServer();
        verify(mockExecutor).shutdown();
        verify(mockExecutor, times(2)).awaitTermination(60, TimeUnit.SECONDS);
        verify(mockExecutor).shutdownNow();
        verify(mockServerSocket).close();
    }
}
