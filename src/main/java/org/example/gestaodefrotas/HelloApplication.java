package org.example.gestaodefrotas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import java.sql.Connection;
import org.example.gestaodefrotas.dao.ConexaoDB;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Connection conn = ConexaoDB.conectar();
            if (conn != null) {
                conn.close();
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Teste de Banco");
                alert.setHeaderText(null);
                alert.setContentText("SUCESSO! Conectado ao MySQL 'frotadb'.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Falha na Conexão");
            alert.setHeaderText("Não foi possível conectar");
            alert.setContentText("Erro: " + e.getMessage());
            alert.showAndWait();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Gestão de Frota");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}