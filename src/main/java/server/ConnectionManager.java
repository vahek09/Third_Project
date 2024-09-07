package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
    static Gson gson = new Gson();
    private RequestHandler requestHandler = new RequestHandler();
    private ExecutorService executor;
    private ServerSocket serverSocket;

    public ConnectionManager() {
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void shutdownServer() throws IOException, InterruptedException {
        if (executor != null) {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(60, TimeUnit.SECONDS);
            }
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public void handleClient(Socket socket) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            String clientMSG = input.readUTF();
            JsonObject request = gson.fromJson(clientMSG, JsonObject.class);

            System.out.println("Received: " + gson.toJson(request));

            String response = requestHandler.processCommand(request);

            System.out.println("Sent: " + response);
            output.writeUTF(response);

            if ("exit".equals(request.get("type").getAsString())) {
                System.out.println("Closing client connection due to 'exit' command.");
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("IOException in handleClient: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception in handleClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
