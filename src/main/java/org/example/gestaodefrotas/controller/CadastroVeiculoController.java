package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent; // <--- O IMPORT CORRETO É ESSE!
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;

public class CadastroVeiculoController {

    @FXML private TextField txtModelo;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtKm;
    @FXML private TextField txtValor;

    @FXML
    protected void onSalvar() {
        try {
            String modelo = txtModelo.getText();
            String placa = txtPlaca.getText();
            int km = Integer.parseInt(txtKm.getText());
            double valor = Double.parseDouble(txtValor.getText());

            Veiculo novoCarro = new Veiculo(modelo, placa, km, valor);

            VeiculoDAO dao = new VeiculoDAO();
            dao.salvar(novoCarro);

            mostrarAlerta("Sucesso", "Veículo cadastrado!");
            limparCampos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "KM e Valor devem ser números!");
        } catch (Exception e) {
            mostrarAlerta("Erro", "Falha ao salvar: " + e.getMessage());
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
    } // <--- FALTAVA ESSA CHAVE AQUI

    private void limparCampos() {
        txtModelo.clear();
        txtPlaca.clear();
        txtKm.clear();
        txtValor.clear();
    }

    // AQUI ESTAVA O ERRO DE DIGITAÇÃO NOS PARÂMETROS
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}