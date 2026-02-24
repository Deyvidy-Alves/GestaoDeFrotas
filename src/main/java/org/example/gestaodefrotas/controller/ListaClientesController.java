package org.example.gestaodefrotas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.model.Cliente;

import java.io.IOException;
import java.sql.SQLException;

public class ListaClientesController {

    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colCnh;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colStatus;

    @FXML
    public void initialize() {
        // PROPERTYVALUEFACTORY: Mapeamento automatico inteligente (lembra da dica pra professora?)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colCnh.setCellValueFactory(new PropertyValueFactory<>("cnhNumero"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        carregarTabela();
    }

    private void carregarTabela() {
        try {
            ObservableList<Cliente> clientes = FXCollections.observableArrayList(new ClienteDAO().listarTodos());
            tabelaClientes.setItems(clientes);
        } catch (SQLException e) {
            mostrarAlerta("erro", "falha ao carregar clientes: " + e.getMessage());
        }
    }

    @FXML
    protected void onEditar(ActionEvent event) {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um cliente na tabela primeiro!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("cadastro-cliente-view.fxml"));
            Parent root = loader.load();

            // Pega o controle e injeta os dados (a magica que voce achou escondida)
            CadastroClienteController controller = loader.getController();
            controller.prepararEdicao(selecionado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onExcluir(ActionEvent event) {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um cliente para inativar!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("confirmar inativação");
        confirmacao.setHeaderText("excluir " + selecionado.getNome() + "?");
        confirmacao.setContentText("o cliente será inativado e sumirá das telas, mas o histórico financeiro continuará intacto.");

        if (confirmacao.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                // aciona a regra de SOFT DELETE
                new ClienteDAO().inativar(selecionado.getId());
                mostrarAlerta("sucesso", "cliente inativado com sucesso!");
                carregarTabela();
            } catch (SQLException e) {
                mostrarAlerta("erro", "falha ao inativar: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}