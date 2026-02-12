package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField; // <--- NÃO ESQUEÇA DESSE IMPORT
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.LocacaoDAO;
import org.example.gestaodefrotas.model.Locacao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DevolucaoController {

    @FXML private ComboBox<Locacao> cbLocacoes;
    @FXML private Label lblResumo;
    @FXML private TextField txtKmDevolucao;// <--- 1. DECLARAMOS O CAMPO AQUI
    @FXML private Label lblKmRetirada;

    @FXML
    public void initialize() {
        carregarLocacoes();
        cbLocacoes.setOnAction(event -> atualizarResumo());
    }

    private void carregarLocacoes() {
        try {
            cbLocacoes.getItems().clear();
            cbLocacoes.getItems().addAll(new LocacaoDAO().listarEmAberto());
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao carregar locações: " + e.getMessage());
        }
    }

    private void atualizarResumo() {
        Locacao loc = cbLocacoes.getValue();
        if (loc != null) {
            // Atualiza o Label do KM de retirada para o usuário ver
            lblKmRetirada.setText("KM na retirada: " + loc.getVeiculo().getKm()); //

            // ... resto da sua lógica de cálculo de dias e multa ...
        } else {
            lblKmRetirada.setText("KM na retirada: --");
        }
    }

    @FXML
    protected void onConfirmar() {
        Locacao loc = cbLocacoes.getValue();
        if (loc == null) {
            mostrarAlerta("Atenção", "Selecione uma locação!");
            return;
        }

        // --- 2. LER E VALIDAR O KM ---
        String textoKm = txtKmDevolucao.getText();
        if (textoKm == null || textoKm.isEmpty()) {
            mostrarAlerta("Erro", "Informe a quilometragem atual do veículo!");
            return;
        }

        int kmDevolucao;
        try {
            kmDevolucao = Integer.parseInt(textoKm);
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "O KM deve ser um número válido!");
            return;
        }

        // Verifica se o cara não tá tentando voltar o hodômetro
        if (kmDevolucao < loc.getVeiculo().getKm()) {
            mostrarAlerta("Erro", "O KM atual (" + kmDevolucao + ") não pode ser menor que o da retirada (" + loc.getVeiculo().getKm() + ")!");
            return;
        }

        // Passa o KM novo para o objeto, pro DAO saber salvar depois
        loc.getVeiculo().setKm(kmDevolucao);
        // -----------------------------

        // Daqui pra baixo é a lógica de devolução que já fizemos (datas e valores)
        try {
            // ... Lógica de multas (pode manter a anterior) ...

            new LocacaoDAO().registrarDevolucao(loc);

            mostrarAlerta("Sucesso", "Veículo devolvido e KM atualizado!");
            carregarLocacoes();
            txtKmDevolucao.clear();

        } catch (SQLException e) {
            mostrarAlerta("Erro", "Falha: " + e.getMessage());
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
        alert.setContentText(msg);
        alert.showAndWait();
    }
}