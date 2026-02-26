package org.example.appmessagerie.server;

import org.example.appmessagerie.dao.MessageDAO;
import org.example.appmessagerie.dao.UserDAO;
import org.example.appmessagerie.entities.Message;
import org.example.appmessagerie.entities.User;
import org.example.appmessagerie.entities.UserStatus;
import org.example.appmessagerie.utils.PasswordUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;
    private final UserDAO userDAO = new UserDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = in.readLine()) != null) {
                handleCommand(input);
            }
        } catch (IOException e) {
            logger.warning("Connexion perdue avec un client : " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleCommand(String input) {
        String[] parts = input.split("\\|");
        String command = parts[0];

        switch (command) {
            case "REGISTER":
                handleRegister(parts[1], parts[2]);
                break;
            case "LOGIN":
                handleLogin(parts[1], parts[2]);
                break;
            case "SEND":
                handleSend(parts[1], parts[2]);
                break;
            case "GET_USERS":
                handleGetUsers();
                break;
            case "GET_HISTORY":
                handleGetHistory(parts[1]);
                break;
            case "LOGOUT":
                disconnect();
                break;
            default:
                out.println("ERROR|Commande inconnue");
        }
    }

    private void handleGetHistory(String otherUsername) {
        if (currentUser == null) return;
        User other = userDAO.findByUsername(otherUsername);
        if (other == null) return;
        
        List<Message> history = messageDAO.findConversation(currentUser, other);
        StringBuilder sb = new StringBuilder("HISTORY");
        for (Message m : history) {
            sb.append("|").append(m.getSender().getUsername()).append(":").append(m.getContent());
        }
        out.println(sb.toString());
    }

    private void handleRegister(String username, String password) {
        if (userDAO.findByUsername(username) != null) {
            out.println("ERROR|Nom d'utilisateur déjà pris");
            return;
        }
        User user = User.builder()
                .username(username)
                .password(PasswordUtil.hashPassword(password))
                .status(UserStatus.OFFLINE)
                .build();
        userDAO.save(user);
        out.println("SUCCESS|Inscription réussie");
        logger.info("Nouvel utilisateur inscrit : " + username);
    }

    private void handleLogin(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null || !PasswordUtil.checkPassword(password, user.getPassword())) {
            out.println("ERROR|Identifiants incorrects");
            return;
        }

        if (ChatManager.getInstance().isUserOnline(username)) {
            out.println("ERROR|Utilisateur déjà connecté");
            return;
        }

        this.currentUser = user;
        this.currentUser.setStatus(UserStatus.ONLINE);
        userDAO.update(this.currentUser);
        ChatManager.getInstance().addClient(username, this);
        
        out.println("LOGIN_SUCCESS|" + username);
        logger.info("Utilisateur connecté : " + username);
        
        // Envoyer les messages en attente
        deliverPendingMessages();
        // Notifier les autres du changement de statut
        broadcastStatusUpdate(username, "ONLINE");
    }

    private void handleSend(String receiverUsername, String content) {
        if (currentUser == null) {
            out.println("ERROR|Non authentifié");
            return;
        }

        User receiver = userDAO.findByUsername(receiverUsername);
        if (receiver == null) {
            out.println("ERROR|Destinataire inexistant");
            return;
        }

        Message message = Message.builder()
                .sender(currentUser)
                .receiver(receiver)
                .content(content)
                .build();
        messageDAO.save(message);

        ClientHandler receiverHandler = ChatManager.getInstance().getClient(receiverUsername);
        if (receiverHandler != null) {
            receiverHandler.out.println("MESSAGE|" + currentUser.getUsername() + "|" + content);
        }
        logger.info("Message de " + currentUser.getUsername() + " vers " + receiverUsername);
    }

    private void handleGetUsers() {
        List<User> users = userDAO.findAll();
        StringBuilder sb = new StringBuilder("USERS_LIST");
        for (User u : users) {
            sb.append("|").append(u.getUsername()).append(":").append(u.getStatus());
        }
        out.println(sb.toString());
    }

    private void deliverPendingMessages() {
        List<Message> pending = messageDAO.findUnreadMessages(currentUser);
        for (Message m : pending) {
            out.println("MESSAGE|" + m.getSender().getUsername() + "|" + m.getContent());
            // Marquer comme reçu (simplifié ici, idéalement on attend un ACK du client)
            m.setStatus(org.example.appmessagerie.entities.MessageStatus.RECU);
            messageDAO.update(m);
        }
    }

    private void broadcastStatusUpdate(String username, String status) {
        for (ClientHandler handler : ChatManager.getInstance().getActiveClients().values()) {
            if (handler != this) {
                handler.out.println("UPDATE_USER|" + username + "|" + status);
            }
        }
    }

    private void disconnect() {
        if (currentUser != null) {
            currentUser.setStatus(UserStatus.OFFLINE);
            userDAO.update(currentUser);
            ChatManager.getInstance().removeClient(currentUser.getUsername());
            broadcastStatusUpdate(currentUser.getUsername(), "OFFLINE");
            logger.info("Utilisateur déconnecté : " + currentUser.getUsername());
            currentUser = null;
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.warning("Erreur fermeture socket : " + e.getMessage());
        }
    }
}
