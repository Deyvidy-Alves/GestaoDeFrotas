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
                alert.setTitle("teste de banco");
                alert.setHeaderText(null);
                alert.setContentText("sucesso! conectado ao mysql 'frotadb'.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("falha na conexão");
            alert.setHeaderText("não foi possível conectar");
            alert.setContentText("erro: " + e.getMessage());
            alert.showAndWait();
        }

        // carrega o arquivo principal de interface
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));

        // cria a cena com as dimensoes fixas de 850 por 650
        Scene scene = new Scene(fxmlLoader.load(), 850, 650);

        // configura as propriedades da janela principal
        stage.setTitle("gestão de frota");
        stage.setScene(scene);

        // trava o tamanho da janela para impedir o redimensionamento pelo usuario
        stage.setResizable(false);

        // desliga o estado maximizado do windows para manter o tamanho fixo
        stage.setMaximized(false);

        // exibe a janela na tela
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}