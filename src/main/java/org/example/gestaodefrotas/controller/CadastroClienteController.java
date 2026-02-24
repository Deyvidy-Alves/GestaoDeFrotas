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
import java.time.LocalDate;

public class CadastroClienteController {

    // ligacao com as caixinhas da tela visual
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtCnh;
    @FXML private TextField txtTelefone;
    @FXML private DatePicker dtValidade;

    // variavel que guarda o cliente se a gente estiver na tela de edicao
    private Cliente clienteEmEdicao = null;

    // injeta os dados antigos na tela para editar
    public void prepararEdicao(Cliente c) {
        this.clienteEmEdicao = c;
        txtNome.setText(c.getNome());
        txtCpf.setText(c.getCpf());
        txtCnh.setText(c.getCnhNumero());
        dtValidade.setValue(c.getCnhValidade());
        txtTelefone.setText(c.getTelefone());
    }

    @FXML
    protected void onSalvar() {
        try {
            // coleta o texto das caixas
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim();
            String cnh = txtCnh.getText().trim();
            String telefone = txtTelefone.getText().trim();
            LocalDate validade = dtValidade.getValue();

            // campos vazios. nenhum campo pode ficar em branco
            if (nome.isEmpty() || cpf.isEmpty() || cnh.isEmpty() || telefone.isEmpty() || validade == null) {
                mostrarAlerta("atenção", "todos os campos são obrigatórios!");
                return;
            }

            // regex: pega o cpf digitado e arranca tudo que nao for numero de 0 a 9 (pontos, tracos, letras)
            String cpfApenasNumeros = cpf.replaceAll("[^0-9]", "");

            //nquantidade de digitos do cpf (tem que ter exatamente 11)
            if (cpfApenasNumeros.length() != 11) {
                mostrarAlerta("erro de validação", "o cpf deve conter exatamente 11 números!");
                return;
            }

            // regex: limpa a cnh deixando so numeros
            String cnhApenasNumeros = cnh.replaceAll("[^0-9]", "");

            // quantidade de digitos da cnh (padrao do detran sao 11 numeros)
            if (cnhApenasNumeros.length() != 11) {
                mostrarAlerta("erro de validação", "a cnh deve conter exatamente 11 números!");
                return;
            }

            // trava 4: cnh vencida. o java compara se a data da carteira vem ANTES da data de hoje
            if (validade.isBefore(LocalDate.now())) {
                mostrarAlerta("erro grave", "não é permitido cadastrar cliente com a cnh vencida!");
                return; // cancela o salvamento para proteger a locadora
            }

            ClienteDAO dao = new ClienteDAO();

            // se for null, e um cliente novo
            if (clienteEmEdicao == null) {
                // cria o objeto cliente limpando as mascaras para salvar padronizado no banco
                Cliente novoCliente = new Cliente(nome, cpfApenasNumeros, cnhApenasNumeros, validade, telefone);
                dao.salvar(novoCliente);
                mostrarAlerta("sucesso", "cliente cadastrado com sucesso!");
            }
            // se nao for null, e uma edicao de um cliente que ja existe
            else {
                clienteEmEdicao.setNome(nome);
                clienteEmEdicao.setCpf(cpfApenasNumeros);
                clienteEmEdicao.setCnhNumero(cnhApenasNumeros);
                clienteEmEdicao.setCnhValidade(validade);
                clienteEmEdicao.setTelefone(telefone);

                dao.atualizar(clienteEmEdicao);
                mostrarAlerta("sucesso", "dados do cliente atualizados!");
            }

            // limpa as caixinhas para o proximo cadastro e reseta a variavel
            limparCampos();
            clienteEmEdicao = null;

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao salvar o cliente: " + e.getMessage());
        }
    }

    // volta pro menu inicial
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

    private void limparCampos() {
        txtNome.clear();
        txtCpf.clear();
        txtCnh.clear();
        txtTelefone.clear();
        dtValidade.setValue(null);
    }

    // exibe o balao com os erros de validacao ou sucesso
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}