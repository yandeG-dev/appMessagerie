module org.example.appmessagerie {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires static lombok;
    requires java.persistence;
    requires jbcrypt;


    opens org.example.appmessagerie to javafx.fxml;
    exports org.example.appmessagerie;
}