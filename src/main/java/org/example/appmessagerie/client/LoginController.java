package org.example.appmessagerie.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private MessageClient client;

    @FXML
    public void initialize() {
        client = SessionManager.getInstance().getClient();
        client.setOnStatusUpdate(status -> {
            Platform.runLater(() -> {
                String[] parts = status.split("\\|");
                if (parts[0].equals("LOGIN_SUCCESS")) {
                    SessionManager.getInstance().setUsername(parts[1]);
                    navigateToChat();
                } else if (parts[0].equals("ERROR")) {
                    statusLabel.setText(parts[1]);
                } else if (parts[0].equals("SUCCESS")) {
                    statusLabel.setText(parts[1]);
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            });
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        try {
            ensureConnected();
            client.send("LOGIN|" + username + "|" + password);
        } catch (IOException e) {
            statusLabel.setText("Erreur de connexion au serveur");
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        try {
            ensureConnected();
            client.send("REGISTER|" + username + "|" + password);
        } catch (IOException e) {
            statusLabel.setText("Erreur de connexion au serveur");
        }
    }

    private void ensureConnected() throws IOException {
        // Idéalement vérifier si déjà connecté
        try {
            client.connect();
        } catch (Exception ignored) {} // Déjà connecté ou erreur gérée
    }

    private void navigateToChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appmessagerie/fxml/chat.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Messagerie - " + SessionManager.getInstance().getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
