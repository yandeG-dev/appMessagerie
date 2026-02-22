module org.example.appmessagerie {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.appmessagerie to javafx.fxml;
    exports org.example.appmessagerie;
}