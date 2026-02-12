package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.dao.LocacaoDAO;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class LocacaoController {

    @FXML private ComboBox<Veiculo> cbVeiculo; // Caixa de seleção de Veículos
    @FXML private ComboBox<Cliente> cbCliente; // Caixa de seleção de Clientes
    @FXML private DatePicker dtRetirada;
    @FXML private DatePicker dtDevolucao;

    @FXML
    public void initialize() {
        try {
            carregarListas();
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Falha ao carregar listas: " + e.getMessage());
        }
    }

    private void carregarListas() throws SQLException {
        // Busca carros DISPONÍVEIS e joga na caixa de seleção
        cbVeiculo.getItems().addAll(new VeiculoDAO().listarDisponiveis());

        // Busca todos os clientes
        cbCliente.getItems().addAll(new ClienteDAO().listarTodos());
    }

    @FXML
    protected void onSalvar() {
        try {
            // Validações
            if (cbVeiculo.getValue() == null) throw new Exception("Selecione um veículo");
            if (cbCliente.getValue() == null) throw new Exception("Selecione um cliente");
            if (dtRetirada.getValue() == null || dtDevolucao.getValue() == null) throw new Exception("Selecione as datas");

            Cliente cliente = cbCliente.getValue();

            if (cliente.getCnhValidade().isBefore(LocalDate.now())) {
                mostrarAlerta("Negado", "Cliente com CNH vencida! Renovação necessária.");
                return; // Para tudo e não deixa salvar
            }

            // Criação do objeto
            Locacao locacao = new Locacao(
                    cbVeiculo.getValue(),
                    cbCliente.getValue(),
                    dtRetirada.getValue(),
                    dtDevolucao.getValue()
            );

            // Salvamento
            new LocacaoDAO().salvar(locacao);

            mostrarAlerta("Sucesso", "Locação realizada com sucesso!");
            limpar();

            // Recarrega a lista de veículos (o que foi alugado tem que sumir da lista)
            cbVeiculo.getItems().clear();
            cbVeiculo.getItems().addAll(new VeiculoDAO().listarDisponiveis());

        } catch (Exception e) {
            mostrarAlerta("Erro", e.getMessage());
        }
    }

    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Gestão de Frota");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void limpar() {
        cbVeiculo.setValue(null);
        cbCliente.setValue(null);
        dtRetirada.setValue(LocalDate.now());
        dtDevolucao.setValue(null);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}