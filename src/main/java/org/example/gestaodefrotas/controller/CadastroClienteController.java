package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.model.Cliente;

import java.io.IOException;

public class CadastroClienteController {
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtCnh;
    @FXML private DatePicker dtValidade;
    @FXML private TextField txtTelefone;

    // variavel que guarda o cliente se a tela for aberta pelo botao de editar
    private Cliente clienteEmEdicao = null;

    // metodo chamado pela tela de listagem para injetar o cliente aqui dentro
    public void prepararEdicao(Cliente c) {
        this.clienteEmEdicao = c;
        // preenche as caixas de texto com os dados antigos para o usuario ver o que esta alterando
        txtNome.setText(c.getNome());
        txtCpf.setText(c.getCpf());
        txtCnh.setText(c.getCnhNumero());
        dtValidade.setValue(c.getCnhValidade());
        txtTelefone.setText(c.getTelefone());
    }

    @FXML
    protected void onSalvar() {
        try {
            ClienteDAO dao = new ClienteDAO();

            if (clienteEmEdicao == null) {
                // fluxo de cadastro novo
                Cliente novoCliente = new Cliente(
                        txtNome.getText(),
                        txtCpf.getText(),
                        txtCnh.getText(),
                        dtValidade.getValue(),
                        txtTelefone.getText()
                );
                dao.salvar(novoCliente);
                mostrarAlerta("sucesso", "cliente cadastrado!");
            } else {
                // fluxo de edicao: atualiza o objeto que ja existia na memoria
                clienteEmEdicao.setNome(txtNome.getText());
                clienteEmEdicao.setCpf(txtCpf.getText());
                clienteEmEdicao.setCnhNumero(txtCnh.getText());
                clienteEmEdicao.setCnhValidade(dtValidade.getValue());
                clienteEmEdicao.setTelefone(txtTelefone.getText());

                // manda o dao dar o update no banco
                dao.atualizar(clienteEmEdicao);
                mostrarAlerta("sucesso", "cliente atualizado com sucesso!");

                // limpa a memoria para a tela voltar ao normal na proxima vez
                clienteEmEdicao = null;
            }

            limpar();
        } catch (Exception e) {
            mostrarAlerta("erro", "erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("gestão de frota");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void limpar() {
        txtNome.clear();
        txtCpf.clear();
        txtCnh.clear();
        txtTelefone.clear();
        dtValidade.setValue(null);
    }
}