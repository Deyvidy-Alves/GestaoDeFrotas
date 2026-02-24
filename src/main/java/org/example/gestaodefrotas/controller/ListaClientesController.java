// pacote dos controladores
package org.example.gestaodefrotas.controller;

// importacoes do javafx e do nosso sistema
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

// classe que controla a tela de listagem de clientes
public class ListaClientesController {

    // mapeia a tabela visual inteira
    @FXML private TableView<Cliente> tabelaClientes;
    // mapeia as colunas individuais
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colCnh;
    @FXML private TableColumn<Cliente, String> colTelefone;

    // metodo que o java chama sozinho ao abrir a tela
    @FXML
    public void initialize() {
        // ensina cada coluna a puxar o dado correto da classe cliente
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colCnh.setCellValueFactory(new PropertyValueFactory<>("cnhNumero"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        // busca os dados e preenche a tabela
        carregarTabela();
    }

    // preenche a tabela puxando do banco
    private void carregarTabela() {
        try {
            // cria a lista que o javafx entende
            ObservableList<Cliente> clientes = FXCollections.observableArrayList();

            // pede pro dao buscar a lista (lembrando que ele so vai trazer os que estao 'ativo')
            clientes.addAll(new ClienteDAO().listarTodos());

            // injeta na tela
            tabelaClientes.setItems(clientes);

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao carregar os clientes: " + e.getMessage());
        }
    }

    // o nosso botao de soft delete
    @FXML
    protected void onExcluir(ActionEvent event) {
        // pega qual cliente o usuario selecionou na grade
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();

        // se ele clicou no botao sem selecionar ninguem, a gente barra
        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um cliente na tabela para excluir!");
            return;
        }

        // cria uma confirmacao pra evitar acidentes
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("confirmar exclusão");
        confirmacao.setHeaderText("excluir " + selecionado.getNome() + "?");
        confirmacao.setContentText("ele será removido da lista, mas seu histórico de locações será mantido no sistema.");

        // se apertou ok, a magica acontece
        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                // chama o metodo inativar que criamos no dao, passando o id do cliente
                new ClienteDAO().inativar(selecionado.getId());

                mostrarAlerta("sucesso", "cliente removido com sucesso!");

                // atualiza a tabela. como ele virou 'inativo', o select nao acha mais ele e ele some da tela
                carregarTabela();

            } catch (Exception e) {
                mostrarAlerta("erro", "falha ao tentar excluir: " + e.getMessage());
            }
        }
    }

    // botao que vai abrir a tela de edicao (vamos ajustar isso depois)
    // acao de clicar no botao editar
    @FXML
    protected void onEditar(ActionEvent event) {
        // pega qual cliente voce selecionou na grade
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();

        // trava de seguranca
        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um cliente para editar!");
            return;
        }

        try {
            // carrega o visual da tela de cadastro de clientes
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("cadastro-cliente-view.fxml"));
            Parent root = loader.load();

            // pega os controles da tela de cadastro
            CadastroClienteController controller = loader.getController();

            // injeta o cliente selecionado la dentro para preencher os campos
            controller.prepararEdicao(selecionado);

            // troca a janela visualmente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // botao pra voltar ao menu principal
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

    // popup de alerta padrao
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}