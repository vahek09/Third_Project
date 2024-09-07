package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMain {
    private static final int PORT = 34567;
    private final ExecutorService executor;
    private final ConnectionManager connectionManager;
    private final ServerSocket serverSocket;

    public ServerMain(ServerSocket serverSocket, ExecutorService executor, ConnectionManager connectionManager) {
        this.serverSocket = serverSocket;
        this.executor = executor;
        this.connectionManager = connectionManager;
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("localhost"))) {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            ConnectionManager connectionManager = new ConnectionManager();
            new ServerMain(serverSocket, executor, connectionManager).startServer();
        }
    }

    public void startServer() throws IOException {
        System.out.println("Server started!");

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> connectionManager.handleClient(socket));
            }
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor did not terminate");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
