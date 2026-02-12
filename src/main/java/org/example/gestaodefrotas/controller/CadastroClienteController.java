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

    @FXML
    protected void onSalvar() {
        try {
            Cliente c = new Cliente(
                    txtNome.getText(),
                    txtCpf.getText(),
                    txtCnh.getText(),
                    dtValidade.getValue(),
                    txtTelefone.getText()
            );

            new ClienteDAO().salvar(c);

            mostrarAlerta("Sucesso", "Cliente cadastrado!");
            limpar();
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar: " + e.getMessage());
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