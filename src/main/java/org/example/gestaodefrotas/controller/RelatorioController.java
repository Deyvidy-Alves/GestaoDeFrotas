// o pacote final
package org.example.gestaodefrotas.controller;

// componentes pro visual e de conexao
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.LocacaoDAO;

import java.io.IOException;
import java.sql.SQLException;

// o responsavel por ler quanto a empresa faturou
public class RelatorioController {

    // rotulo onde vai aparecer o textao do dinheiro
    @FXML private Label lblFaturamento;

    // executado automaticamente na abertura
    @FXML
    public void initialize() {
        try {
            // chama a nossa locacaodao que tem aquele metodo com select sum()
            double total = new LocacaoDAO().calcularFaturamentoTotal();

            // converte o que era um "1200.5" solto e sem graca para um lindo "r$ 1.200,50" e joga na tela
            lblFaturamento.setText(String.format("r$ %.2f", total));

        } catch (SQLException e) {
            // se o banco nao responder, mostra mensagem generica de erro em vez do valor
            lblFaturamento.setText("erro ao carregar");
        }
    }

    // o botao cinza classio pra vazar de la
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // pega a visao do menu
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // pega o palco
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // pendura a visao do menu no palco
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}