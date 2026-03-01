module org.example.appmessagerie {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens org.example.appmessagerie to javafx.fxml;
    exports org.example.appmessagerie;
}