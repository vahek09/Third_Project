package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ServerMainTest {

    private ServerMain serverMain;
    private ServerSocket mockServerSocket;
    private ExecutorService executor;
    private ConnectionManager mockConnectionManager;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() throws IOException {
        mockServerSocket = mock(ServerSocket.class);
        executor = Executors.newFixedThreadPool(10);
        mockConnectionManager = mock(ConnectionManager.class);

        // Initialize ServerMain with mocks
        serverMain = new ServerMain(mockServerSocket, executor, mockConnectionManager);

        // Setup for capturing System.out
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    @DisplayName("Test Start Server Method")
    void testStartServer() throws IOException, InterruptedException {
        // Arrange
        Socket mockSocket = mock(Socket.class);

        final int[] acceptCounter = {0};
        when(mockServerSocket.accept()).thenAnswer(new Answer<Socket>() {
            @Override
            public Socket answer(InvocationOnMock invocation) throws Throwable {
                if (acceptCounter[0]++ < 2) {
                    return mockSocket;
                } else {
                    throw new IOException("Stop the loop");
                }
            }
        });

        Thread serverThread = new Thread(() -> {
            try {
                serverMain.startServer();
            } catch (IOException e) {
                System.out.println("Expected exception to break loop: " + e.getMessage());
            }
        });
        serverThread.start();
        serverThread.join(1000);

        verify(mockServerSocket, times(3)).accept();
        verify(mockConnectionManager, times(2)).handleClient(mockSocket);

        serverMain.shutdown();
        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("Test Shutdown Method")
    void testShutdown() throws InterruptedException {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(anyLong(), any())).thenReturn(true);
        ServerMain serverMainWithMockExecutor = new ServerMain(mockServerSocket, mockExecutor, mockConnectionManager);

        serverMainWithMockExecutor.shutdown();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(60, TimeUnit.SECONDS);
        verify(mockExecutor, never()).shutdownNow();
    }

    @Test
    @DisplayName("Test Shutdown with ShutdownNow")
    void testShutdownWithShutdownNow() throws InterruptedException {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(anyLong(), any()))
                .thenReturn(false)
                .thenReturn(true);

        ServerMain serverMainWithMockExecutor = new ServerMain(mockServerSocket, mockExecutor, mockConnectionManager);

        serverMainWithMockExecutor.shutdown();

        verify(mockExecutor).shutdown();
        verify(mockExecutor, times(2)).awaitTermination(60, TimeUnit.SECONDS);
        verify(mockExecutor).shutdownNow();
    }

    @Test
    @DisplayName("Test Main Method")
    void testMainMethod() throws IOException, InterruptedException {
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        ExecutorService mockExecutor = Executors.newFixedThreadPool(10);
        when(mockServerSocket.accept()).thenAnswer(invocation -> {
            throw new IOException("Simulated accept exception to break loop.");
        });

        Thread mainThread = new Thread(() -> {
            try {
                ServerMain.main(new String[0]);
            } catch (IOException e) {
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
        mainThread.start();

        Thread.sleep(500);

        String output = outContent.toString();
        assertTrue(output.contains("Server started!"), "Server should print 'Server started!' on startup.");

        mockExecutor.shutdownNow();
        mainThread.join(1000);
    }

    @Test
    @DisplayName("Test ServerMain StartServer with Exception")
    void testServerMainStartServer() throws IOException, InterruptedException {
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        ExecutorService mockExecutor = mock(ExecutorService.class);
        ConnectionManager mockConnectionManager = mock(ConnectionManager.class);

        ServerMain testServerMain = new ServerMain(mockServerSocket, mockExecutor, mockConnectionManager);
        when(mockServerSocket.accept()).thenThrow(new IOException("Simulated accept exception"));

        Thread serverThread = new Thread(() -> {
            try {
                testServerMain.startServer();
            } catch (IOException e) {
                System.out.println("Expected exception: " + e.getMessage());
            }
        });
        serverThread.start();
        serverThread.join(1000);
        verify(mockServerSocket, times(1)).accept();
        verify(mockConnectionManager, never()).handleClient(any());
        testServerMain.shutdown();
    }
}
