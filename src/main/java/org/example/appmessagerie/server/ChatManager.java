package org.example.appmessagerie.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    private static ChatManager instance;
    private final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    private ChatManager() {}

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void addClient(String username, ClientHandler handler) {
        activeClients.put(username, handler);
    }

    public void removeClient(String username) {
        activeClients.remove(username);
    }

    public ClientHandler getClient(String username) {
        return activeClients.get(username);
    }

    public Map<String, ClientHandler> getActiveClients() {
        return activeClients;
    }
    
    public boolean isUserOnline(String username) {
        return activeClients.containsKey(username);
    }
}
