package org.example.appmessagerie.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class MessageClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String serverAddress = "localhost";
    private int port = 8888;
    private Consumer<String> onMessageReceived;
    private Consumer<String> onStatusUpdate;
    private boolean running = false;

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnStatusUpdate(Consumer<String> callback) {
        this.onStatusUpdate = callback;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        running = true;

        new Thread(() -> {
            try {
                String response;
                while (running && (response = in.readLine()) != null) {
                    handleResponse(response);
                }
            } catch (IOException e) {
                if (running) {
                    onStatusUpdate.accept("DISCONNECTED|Connexion perdue");
                }
            }
        }).start();
    }

    private void handleResponse(String response) {
        String[] parts = response.split("\\|");
        String type = parts[0];

        if (type.equals("MESSAGE") && onMessageReceived != null) {
            onMessageReceived.accept(parts[1] + ": " + parts[2]);
        } else if (onStatusUpdate != null) {
            onStatusUpdate.accept(response);
        }
    }

    public void send(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
