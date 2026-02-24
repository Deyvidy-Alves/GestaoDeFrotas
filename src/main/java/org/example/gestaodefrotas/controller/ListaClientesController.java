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

    // amarrando as colunas da tela com o codigo
    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colCnh;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colStatus;

    @FXML
    public void initialize() {
        // reflection: mapeia automaticamente as colunas da tela com a classe cliente
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
            // carrega a lista atualizada mostrando os ativos e os inativos
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

            // injeta os dados do cliente selecionado na tela de cadastro para poder editar
            CadastroClienteController controller = loader.getController();
            controller.prepararEdicao(selecionado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // regra de exclusao usando a nomenclatura correta
    @FXML
    protected void onExcluir(ActionEvent event) {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um cliente para excluir!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("confirmar exclusão");
        confirmacao.setHeaderText("excluir " + selecionado.getNome() + "?");
        confirmacao.setContentText("o cliente será removido desta lista, mas o histórico financeiro continuará intacto no banco.");

        if (confirmacao.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                // aciona o dao para mudar o status para 'excluido'
                new ClienteDAO().excluir(selecionado.getId());
                mostrarAlerta("sucesso", "cliente excluído com sucesso!");
                carregarTabela();
            } catch (SQLException e) {
                mostrarAlerta("erro", "falha ao excluir: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // localiza o arquivo de design do menu principal
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // identifica a janela atual onde o botao foi clicado
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // desliga o estado maximizado para que a janela nao aumente sozinha ao voltar
            stage.setMaximized(false);

            // gruda a cena do menu na janela
            stage.setScene(scene);

            // estabelece a largura e altura padronizadas do sistema
            stage.setWidth(850);
            stage.setHeight(650);

            // impede o usuario de redimensionar a janela manualmente
            stage.setResizable(false);

            // reposiciona a janela no centro do monitor
            stage.centerOnScreen();

            // exibe as alteracoes na tela
            stage.show();
        } catch (IOException e) {
            // exibe o rastreio do erro caso o carregamento do fxml falhe
            e.printStackTrace();
        }
    }

    // balaozinho padrao
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}