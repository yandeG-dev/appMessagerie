package org.example.appmessagerie.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerLauncher {
    private static final int PORT = 8888;
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Serveur de messagerie démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Nouvelle connexion acceptée : " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            logger.severe("Erreur serveur : " + e.getMessage());
        }
    }
}
