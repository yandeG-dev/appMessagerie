package org.example.appmessagerie.serveur.config;
import java.net.*;
import java.util.*;

public class ServeurConfig {


    public class Server {

        // Liste de tous les clients connectés
        // clé = username, valeur = son ClientHandler
        public static Map<String, ClientHandler> clientsConnectes = new HashMap<>();

        public static void main(String[] args) {
            try {
                ServerSocket serverSocket = new ServerSocket(8080);
                System.out.println("✅ Serveur démarré sur le port 8080...");

                while (true) {
                    // Attend qu'un client arrive
                    Socket socket = serverSocket.accept();
                    System.out.println("🔗 Nouveau client connecté : " + socket.getInetAddress());

                    // Crée un thread dédié pour ce client
                    ClientHandler handler = new ClientHandler(socket);
                    new Thread(handler).start();
                }

            } catch (Exception e) {
                System.out.println("❌ Erreur serveur : " + e.getMessage());
            }
        }
    }
}
