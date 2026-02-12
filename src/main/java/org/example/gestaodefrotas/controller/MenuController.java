package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import java.io.IOException;

public class MenuController {

    @FXML
    protected void irParaVeiculos(ActionEvent event) {
        trocarTela(event, "cadastro-veiculo-view.fxml", "Novo Veículo");
    }

    @FXML
    protected void irParaClientes(ActionEvent event) {
        trocarTela(event, "cadastro-cliente-view.fxml", "Novo Cliente");
    }

    @FXML
    protected void irParaLocacao(ActionEvent event) {
        trocarTela(event, "locacao-view.fxml", "Nova Locação");
    }

    @FXML
    protected void sair() {
        System.exit(0);
    }

    @FXML
    protected void onAbrirDevolucao(ActionEvent event) throws IOException {
        trocarTela(event, "devolucao-view.fxml", "Devolução de Veículo");
    }

    private void trocarTela(ActionEvent event, String nomeArquivoFxml, String titulo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(nomeArquivoFxml));

            if (fxmlLoader.getLocation() == null) {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/gestaodefrotas/" + nomeArquivoFxml));
            }

            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}