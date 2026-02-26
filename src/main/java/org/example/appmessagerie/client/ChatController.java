package org.example.appmessagerie.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatController {

    @FXML private ListView<String> userListView;
    @FXML private ListView<MessageDisplay> messageListView;
    @FXML private TextField messageInput;
    @FXML private Label chatTitleLabel;

    private MessageClient client;
    private final ObservableList<String> users = FXCollections.observableArrayList();
    private final Map<String, ObservableList<MessageDisplay>> conversations = new HashMap<>();
    private String selectedUser;

    @FXML
    public void initialize() {
        client = SessionManager.getInstance().getClient();
        userListView.setItems(users);
        
        // Configuration du design des bulles de message (Style WhatsApp)
        messageListView.setCellFactory(param -> new ListCell<MessageDisplay>() {
            @FXML
            protected void updateItem(MessageDisplay item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox container = new HBox();
                    Label messageLabel = new Label(item.getContent());
                    messageLabel.setWrapText(true);
                    messageLabel.setMaxWidth(400);
                    messageLabel.setPadding(new Insets(8, 12, 8, 12));
                    
                    if (item.isMine()) {
                        // Mes messages (à droite, fond vert)
                        container.setAlignment(Pos.CENTER_RIGHT);
                        messageLabel.setStyle("-fx-background-color: #dcf8c6; -fx-background-radius: 15 15 0 15; -fx-text-fill: black;");
                    } else {
                        // Messages reçus (à gauche, fond blanc)
                        container.setAlignment(Pos.CENTER_LEFT);
                        messageLabel.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15 15 15 0; -fx-text-fill: black;");
                    }
                    
                    container.getChildren().add(messageLabel);
                    container.setPadding(new Insets(5, 10, 5, 10));
                    setGraphic(container);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal.split(":")[0];
                chatTitleLabel.setText("Chat avec " + selectedUser);
                loadConversation(selectedUser);
                client.send("GET_HISTORY|" + selectedUser);
            }
        });

        client.setOnMessageReceived(message -> {
            Platform.runLater(() -> {
                String[] parts = message.split(": ");
                String sender = parts[0];
                String content = parts[1];
                
                boolean isMine = sender.equals(SessionManager.getInstance().getUsername());
                addMessageToConversation(sender, content, isMine);
            });
        });

        client.setOnStatusUpdate(status -> {
            Platform.runLater(() -> {
                String[] parts = status.split("\\|");
                switch (parts[0]) {
                    case "HISTORY":
                        if (conversations.get(selectedUser) != null) {
                            conversations.get(selectedUser).clear();
                            for (int i = 1; i < parts.length; i++) {
                                String msg = parts[i];
                                int sepIndex = msg.indexOf(":");
                                if (sepIndex != -1) {
                                    String sender = msg.substring(0, sepIndex);
                                    String content = msg.substring(sepIndex + 1);
                                    boolean isMine = sender.equals(SessionManager.getInstance().getUsername());
                                    conversations.get(selectedUser).add(new MessageDisplay(sender, content, isMine));
                                }
                            }
                        }
                        break;
                    case "USERS_LIST":
                        users.clear();
                        for (int i = 1; i < parts.length; i++) {
                            if (!parts[i].startsWith(SessionManager.getInstance().getUsername())) {
                                users.add(parts[i]);
                            }
                        }
                        break;
                    case "UPDATE_USER":
                        updateUserStatus(parts[1], parts[2]);
                        break;
                    case "DISCONNECTED":
                        handleLogout();
                        break;
                }
            });
        });

        client.send("GET_USERS");
    }

    private void loadConversation(String username) {
        if (!conversations.containsKey(username)) {
            conversations.put(username, FXCollections.observableArrayList());
        }
        messageListView.setItems(conversations.get(username));
    }

    private void addMessageToConversation(String sender, String content, boolean isMine) {
        String convKey = isMine ? selectedUser : sender;
        if (convKey == null) return;
        
        if (!conversations.containsKey(convKey)) {
            conversations.put(convKey, FXCollections.observableArrayList());
        }
        conversations.get(convKey).add(new MessageDisplay(sender, content, isMine));
        
        // Scroll to bottom
        Platform.runLater(() -> messageListView.scrollTo(messageListView.getItems().size() - 1));
    }

    private void updateUserStatus(String username, String status) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).startsWith(username)) {
                users.set(i, username + ":" + status);
                return;
            }
        }
        users.add(username + ":" + status);
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText();
        if (content.isEmpty() || selectedUser == null) return;

        client.send("SEND|" + selectedUser + "|" + content);
        addMessageToConversation(SessionManager.getInstance().getUsername(), content, true);
        messageInput.clear();
    }

    @FXML
    private void handleLogout() {
        client.send("LOGOUT");
        client.disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appmessagerie/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
