package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;
import java.sql.SQLException;

// controlador da oficina meccanica virtual
public class ManutencaoController {

    // conecta com aquela listinha simples onde clicamos nos carros quebrados
    @FXML private ListView<Veiculo> lvVeiculos;

    // executado automaticamente na abertura da tela
    @FXML
    public void initialize() {
        // vai no banco e pega a fila de conserto
        carregarLista();
    }

    private void carregarLista() {
        try {
            // esvazia a fila pra nao dobrar os itens na tela
            lvVeiculos.getItems().clear();

            // puxa do dao os carros em manutencao e empurra eles pra dentro do listview
            lvVeiculos.getItems().addAll(new VeiculoDAO().listarEmManutencao());

        } catch (SQLException e) {
            // se o mysql der erro, a gente mostra a mensagem na tela
            mostrarAlerta("erro", "falha ao buscar veículos na oficina: " + e.getMessage());
        }
    }

    // o botao verde de "carro consertado"
    @FXML
    protected void onLiberar() {
        // ve em qual carro da lista o mouse do mecanico deu um clique
        Veiculo veiculoSelecionado = lvVeiculos.getSelectionModel().getSelectedItem();

        // protecao caso o usuario clique sem selecionar ninguem
        if (veiculoSelecionado == null) {
            mostrarAlerta("aviso", "selecione um veículo na lista primeiro.");
            return;
        }

        try {
            // o ajuste mestre! passamos apenas o id para o dao resetar o ciclo de revisao
            // isso resolve o erro de 'cannot find symbol' que estava dando na build
            new VeiculoDAO().finalizarManutencao(veiculoSelecionado.getId());

            // confirma o sucesso
            mostrarAlerta("sucesso", "o veículo " + veiculoSelecionado.getModelo() + " foi liberado para locação!");

            // atualiza a fila para o carro sumir da oficina
            carregarLista();

        } catch (SQLException e) {
            // trata erros de conexao com o banco
            mostrarAlerta("erro", "erro ao tentar liberar o veículo: " + e.getMessage());
        }
    }

    // volta para o menu inicial travando o tamanho da janela
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // aplica a trava de 850x650 que combinamos para a tela nao pular
            stage.setScene(scene);
            stage.setWidth(850);
            stage.setHeight(650);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // componente de mensagem popup para o usuario
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}