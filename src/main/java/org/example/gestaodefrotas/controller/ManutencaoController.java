// endereco da classe
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

    // auto carregado
    @FXML
    public void initialize() {
        // vai no banco e pega a fila de conserto
        carregarLista();
    }

    private void carregarLista() {
        try {
            // esvazia a fila pra nao dobrar
            lvVeiculos.getItems().clear();

            // puxa do dao os carros em manutencao e empurra eles pra dentro do listview
            lvVeiculos.getItems().addAll(new VeiculoDAO().listarEmManutencao());

        } catch (SQLException e) {
            // se o mysql chorar, a gente mostra a lagrima
            mostrarAlerta("erro", "falha ao buscar veículos na oficina: " + e.getMessage());
        }
    }

    // o botao verde de "carro consertado"
    @FXML
    protected void onLiberar() {
        // passo 1: ve em qual carro da lista o mouse do mecanico deu um clique
        Veiculo veiculoSelecionado = lvVeiculos.getSelectionModel().getSelectedItem();

        // passo 2: se ele for ansioso e clicar antes de selecionar
        if (veiculoSelecionado == null) {
            mostrarAlerta("aviso", "selecione um veículo na lista primeiro.");
            return; // expulsa o java daqui e nao tenta gravar null no banco
        }

        try {
            // passo 3: avisa o veiculodao pra fazer update status = disponivel la no banco de dados
            new VeiculoDAO().finalizarManutencao(veiculoSelecionado);

            // passo 4: festa!
            mostrarAlerta("sucesso", "o veículo " + veiculoSelecionado.getModelo() + " foi liberado e já está disponível para locação!");

            // passo 5: atualiza a fila. o carro que foi consertado some misteriosamente da tela
            carregarLista();

        } catch (SQLException e) {
            // erro de conexao ou de sql
            mostrarAlerta("erro", "erro ao tentar liberar o veículo: " + e.getMessage());
        }
    }

    // de volta a nave mae
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // busca o molde
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // pega a janela
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // gruda
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // caixinha basica
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}