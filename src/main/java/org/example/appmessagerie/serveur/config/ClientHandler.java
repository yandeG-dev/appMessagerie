package org.example.appmessagerie.serveur.config;


import java.io.PrintWriter;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;      // pour envoyer au client
    private BufferedReader in;    // pour recevoir du client
    private String username;      // le nom de l'utilisateur connecté

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Prépare les flux de communication
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String ligne;
            // Boucle infinie : écoute en permanence ce que le client envoie
            while ((ligne = in.readLine()) != null) {
                traiter(ligne);
            }

        } catch (Exception e) {
            // Connexion perdue brutalement (RG10)
            passerOffline();
        }
    }

    // Analyse la commande reçue et agit en conséquence
    private void traiter(String ligne) {
        String[] parties = ligne.split(" ", 3);
        String commande  = parties[0];

        switch (commande) {
            case "LOGIN"       -> gererLogin(parties);
            case "REGISTER"    -> gererInscription(parties);
            case "MESSAGE"     -> gererMessage(parties);
            case "LOGOUT"      -> gererLogout();
            case "GET_USERS"   -> envoyerListeUtilisateurs();
            case "GET_HISTORY" -> envoyerHistorique(parties[1]);
        }
    }

    // ─── LOGIN ───────────────────────────────────────────
    private void gererLogin(String[] parties) {
        String username = parties[1];
        String password = parties[2];

        UserDAO userDAO = new UserDAO();
        User user = userDAO.trouverParUsername(username);

        if (user == null) {
            out.println("LOGIN_FAIL Utilisateur introuvable");
            return;
        }

        // Vérifie le mot de passe haché (RG9)
        if (!PasswordUtil.verifier(password, user.getPassword())) {
            out.println("LOGIN_FAIL Mot de passe incorrect");
            return;
        }

        // Vérifie si déjà connecté (RG3)
        if (Server.clientsConnectes.containsKey(username)) {
            out.println("LOGIN_FAIL Utilisateur déjà connecté");
            return;
        }

        // Connexion OK
        this.username = username;
        Server.clientsConnectes.put(username, this);
        userDAO.mettreAJourStatut(username, "ONLINE"); // RG4
        ServerLogger.log("CONNEXION - " + username);

        out.println("LOGIN_OK");

        // Envoie les messages reçus pendant qu'il était offline (RG6)
        envoyerMessagesEnAttente();
    }

    // ─── INSCRIPTION ─────────────────────────────────────
    private void gererInscription(String[] parties) {
        String username = parties[1];
        String password = parties[2];

        UserDAO userDAO = new UserDAO();

        // Vérifie que le username est unique (RG1)
        if (userDAO.trouverParUsername(username) != null) {
            out.println("REGISTER_FAIL Username déjà pris");
            return;
        }

        // Crée le compte avec mot de passe haché (RG9)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(PasswordUtil.hacher(password));
        newUser.setStatus("OFFLINE");
        newUser.setDateCreation(LocalDateTime.now());

        userDAO.sauvegarder(newUser);
        out.println("REGISTER_OK");
    }

    // ─── MESSAGE ─────────────────────────────────────────
    private void gererMessage(String[] parties) {
        String destinataire = parties[1];
        String contenu      = parties[2];

        // RG7 : contenu pas vide et max 1000 caractères
        if (contenu == null || contenu.isEmpty()) {
            out.println("MESSAGE_FAIL Contenu vide");
            return;
        }
        if (contenu.length() > 1000) {
            out.println("MESSAGE_FAIL Message trop long");
            return;
        }

        UserDAO userDAO = new UserDAO();

        // RG5 : le destinataire doit exister
        if (userDAO.trouverParUsername(destinataire) == null) {
            out.println("MESSAGE_FAIL Destinataire introuvable");
            return;
        }

        // Sauvegarde le message en base
        Message messageDAO = new MessageDAO();
        Message message = new Message();
        message.setSender(this.username);
        message.setReceiver(destinataire);
        message.setContenu(contenu);
        message.setDateEnvoi(LocalDateTime.now());
        message.setStatut("ENVOYE");
        messageDAO.sauvegarder(message);

        ServerLogger.log("MESSAGE - " + this.username + " → " + destinataire);

        // Envoie en temps réel si le destinataire est connecté (RG6)
        MessageRouter.envoyer(destinataire, "MESSAGE " + this.username + " " + contenu);

        out.println("MESSAGE_OK");
    }

    // ─── LOGOUT ──────────────────────────────────────────
    private void gererLogout() {
        passerOffline();
        out.println("LOGOUT_OK");
    }

    // ─── LISTE DES UTILISATEURS ──────────────────────────
    private void envoyerListeUtilisateurs() {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.tousLesUtilisateurs();

        StringBuilder sb = new StringBuilder("USERS ");
        for (User u : users) {
            sb.append(u.getUsername())
                    .append(":")
                    .append(u.getStatus())
                    .append(",");
        }
        out.println(sb.toString());
    }

    // ─── HISTORIQUE ──────────────────────────────────────
    private void envoyerHistorique(String autreUser) {
        MessageDAO messageDAO = new MessageDAO();
        List<Message> messages = messageDAO.getHistorique(this.username, autreUser); // RG8

        for (Message m : messages) {
            out.println("HISTORY " + m.getSender() + " " + m.getContenu() + " " + m.getDateEnvoi());
        }
        out.println("HISTORY_END");
    }

    // ─── MESSAGES EN ATTENTE ─────────────────────────────
    private void envoyerMessagesEnAttente() {
        MessageDAO messageDAO = new MessageDAO();
        List<Message> attente = messageDAO.getMessagesEnAttente(this.username);

        for (Message m : attente) {
            out.println("MESSAGE " + m.getSender() + " " + m.getContenu());
            messageDAO.mettreAJourStatut(m.getId(), "RECU");
        }
    }

    // ─── PASSER OFFLINE ──────────────────────────────────
    private void passerOffline() {
        if (this.username != null) {
            Server.clientsConnectes.remove(this.username);
            new UserDAO().mettreAJourStatut(this.username, "OFFLINE"); // RG4
            ServerLogger.log("DÉCONNEXION - " + this.username);
        }
        try { socket.close(); } catch (Exception e) {}
    }

    // Permet à MessageRouter d'envoyer un message à ce client
    public void envoyerAuClient(String message) {
        out.println(message);
    }
}
