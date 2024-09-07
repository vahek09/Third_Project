package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static server.Constants.*;

public class Main {

    final CommandLineArgs cmdArgs = new CommandLineArgs();
    private static final Gson gson = new Gson();
    private static final String localhost = "localhost";
    private static final int port = 34567;


    public static void main(String[] args) {
        Main client = new Main();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(client.cmdArgs)
                .build();

        try {
            jCommander.parse(args);

        } catch (ParameterException e) {
            System.out.println("Error parsing command-line arguments: " + e.getMessage());
            e.printStackTrace();
            jCommander.usage();
            return;
        }

        client.run();
    }

    public void run(){


        if (cmdArgs.getInputFile() != null)
            handleFileInput(localhost, port, cmdArgs.getInputFile());
        else
            handleSingleRequest(localhost, port);

    }

    void handleFileInput(String address, int port, String inputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(inputFile).getFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String message = line.trim();
                if (!message.isEmpty()) {
                    sendRequest(address, port, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleSingleRequest(String address, int port) {
        String message = buildMessage();
        sendRequest(address, port, message);
    }

    public void sendRequest(String address, int port, String message) {
        try (Socket socket = new Socket(InetAddress.getByName(address), port);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            output.writeUTF(message);
            System.out.println("Sent: " + message);

            String response = input.readUTF();
            System.out.println("Received: " + response);

        } catch (EOFException e) {
            System.err.println("Server closed the connection unexpectedly: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error in communication with server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected String buildMessage() {
        JsonObject request = new JsonObject();
        request.addProperty(KEY_TYPE, cmdArgs.getType());
        request.addProperty(KEY_KEY, cmdArgs.getKey());

        if (TYPE_SET.equals(cmdArgs.getType())) {
            request.addProperty(KEY_VALUE, cmdArgs.getValue());
        }

        return gson.toJson(request);
    }


}