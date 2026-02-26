package org.example.appmessagerie.client;

public class SessionManager {
    private static SessionManager instance;
    private MessageClient client;
    private String username;

    private SessionManager() {
        client = new MessageClient();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public MessageClient getClient() {
        return client;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
