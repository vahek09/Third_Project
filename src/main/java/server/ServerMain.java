
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
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ConnectionManager connectionManager = new ConnectionManager();

    public static void main(String[] args) throws IOException {
        new ServerMain().startServer();
    }

    public void startServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("localhost"))) {
            System.out.println("Server started!");

            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> connectionManager.handleClient(socket));
            }
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
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
