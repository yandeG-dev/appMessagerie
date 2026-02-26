# 📱 Instant Messaging App - Documentation Complète

Cette documentation détaille l'architecture, le fonctionnement technique et les procédures d'installation de l'application de messagerie instantanée (Clone WhatsApp).

---

## 🏗️ 1. Architecture du Projet

L'application suit un modèle **Client-Serveur** strict, utilisant l'API **Java Sockets** pour la communication en temps réel et **JavaFX** pour l'interface graphique.

### A. Structure des Paquetages
*   `org.example.appmessagerie.entities` : Modèles de données JPA (User, Message).
*   `org.example.appmessagerie.dao` : Classes d'accès aux données (Layer de persistance).
*   `org.example.appmessagerie.server` : Logique du serveur multithreadé.
*   `org.example.appmessagerie.client` : Logique client (Socket et contrôleurs UI).
*   `org.example.appmessagerie.utils` : Utilitaires (Hachage de mot de passe, Gestion JPA).

---

## 🌐 2. Fonctionnement Réseau

Le réseau repose sur le protocole **TCP** via les Sockets Java.

### Le Serveur (`ServerLauncher`)
1.  Écoute sur le port **8888**.
2.  Pour chaque nouveau client, il crée un fil d'exécution séparé (`ClientHandler`).
3.  Utilise un protocole texte simple pour communiquer (ex: `SEND|destinataire|contenu`).

### Le Client (`MessageClient`)
1.  Se connecte à l'adresse IP du serveur.
2.  Maintient une boucle d'écoute en arrière-plan pour recevoir les messages sans bloquer l'interface.
3.  Communique avec l'UI via des *callbacks* (Platform.runLater) pour garantir la sécurité des threads JavaFX.

---

## 💾 3. Persistance des Données

Le projet utilise **Hibernate / JPA** pour la gestion de la base de données **PostgreSQL**.

*   **Identité** : Les mots de passe sont hachés avec **BCrypt** avant stockage.
*   **Historique** : Tous les messages sont persistés en base. Le serveur récupère l'historique lors de la sélection d'un utilisateur.
*   **Messages Hors-ligne** : Si un destinataire est déconnecté, le message est stocké avec le statut `ENVOYE` et lui est délivré à sa prochaine connexion.

---

## 🎨 4. Interface Utilisateur (UI)

L'interface est construite avec **JavaFX 21** et **FXML**.
*   **Style WhatsApp** : Utilisation d'une `CellFactory` personnalisée pour afficher les bulles de messages (Vert à droite pour "Moi", Blanc à gauche pour "Lui").
*   **Scene Builder** : Les fichiers FXML sont éditables visuellement. La liaison se fait via l'attribut `fx:id` et les méthodes `onAction`.

---

## 🚀 5. Installation et Lancement

### Prérequis
*   **Java 17** ou supérieur.
*   **PostgreSQL** installé avec une base nommée `appMessagerie`.
*   **Maven** pour la gestion des dépendances.

### Étapes de lancement
1.  **Base de données** : Créer la base `appMessagerie`. Vérifier l'utilisateur/mot de passe dans `persistence.xml`.
2.  **Serveur** : Exécuter `ServerLauncher.main()`.
3.  **Client** : Exécuter `HelloApplication.main()`.

### Utilisation Multi-Machines
Pour chater entre deux ordinateurs :
1.  Récupérer l'IP du serveur via `ipconfig`.
2.  Modifier `MessageClient.java` (ligne 11) avec cette IP sur toutes les machines clientes.

---

## 🛠️ 6. Technologies utilisées
*   **Langage** : Java 17+
*   **UI** : JavaFX & Scene Builder
*   **Database** : PostgreSQL
*   **ORM** : Hibernate / JPA
*   **Sécurité** : BCrypt
*   **Build** : Maven
