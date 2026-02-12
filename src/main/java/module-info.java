module org.example.gestaodefrotas {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;

    opens org.example.gestaodefrotas to javafx.fxml;
    opens org.example.gestaodefrotas.controller to javafx.fxml;
    exports org.example.gestaodefrotas;
}