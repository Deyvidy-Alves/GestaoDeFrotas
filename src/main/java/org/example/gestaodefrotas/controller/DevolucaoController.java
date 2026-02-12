package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

    @FXML
    public void initialize() {
        carregarLocacoes();

        // Quando o usuário escolhe uma locação na lista, a gente atualiza o texto do resumo
        cbLocacoes.setOnAction(event -> atualizarResumo());
    }

    private void carregarLocacoes() {
        try {
            cbLocacoes.getItems().clear();
            // Chama aquele método novo que criamos no DAO
            cbLocacoes.getItems().addAll(new LocacaoDAO().listarEmAberto());
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao carregar locações: " + e.getMessage());
        }
    }

    private void atualizarResumo() {
        Locacao loc = cbLocacoes.getValue();
        if (loc != null) {
            LocalDate hoje = LocalDate.now();

            // 1. Cálculo dos dias normais (da retirada até a PREVISTA ou HOJE, o que for menor)
            long diasTotais = ChronoUnit.DAYS.between(loc.getDataRetirada(), hoje);
            if (diasTotais == 0) diasTotais = 1;

            // 2. Verifica se houve atraso
            long diasAtraso = 0;
            if (hoje.isAfter(loc.getDataDevolucaoPrevista())) {
                diasAtraso = ChronoUnit.DAYS.between(loc.getDataDevolucaoPrevista(), hoje);
            }

            double valorDiaria = loc.getVeiculo().getValorDiaria();
            double valorNormal = diasTotais * valorDiaria;

            // Multa: R$ 50,00 fixo + valor da diária extra
            double multa = diasAtraso > 0 ? (diasAtraso * valorDiaria) + 50.0 : 0.0;

            double totalFinal = valorNormal + multa;

            String texto = String.format(
                    "Veículo: %s\n" +
                            "Dias Totais: %d\n" +
                            "Dias de Atraso: %d\n" +
                            "Valor Diárias: R$ %.2f\n" +
                            "Multa por Atraso: R$ %.2f\n" +
                            "--------------------\n" +
                            "TOTAL A PAGAR: R$ %.2f",
                    loc.getVeiculo().getModelo(),
                    diasTotais, diasAtraso, valorNormal, multa, totalFinal
            );

            lblResumo.setText(texto);
        }
    }

    @FXML
    protected void onConfirmar() {
        Locacao loc = cbLocacoes.getValue();
        if (loc == null) {
            mostrarAlerta("Atenção", "Selecione uma locação para devolver!");
            return;
        }

        try {
            new LocacaoDAO().registrarDevolucao(loc);

            // --- CÁLCULO SÓ PARA MOSTRAR NA MENSAGEM ---
            long dias = ChronoUnit.DAYS.between(loc.getDataRetirada(), LocalDate.now());
            if (dias == 0) dias = 1;
            double valorCobrado = dias * loc.getVeiculo().getValorDiaria();
            // -------------------------------------------

            mostrarAlerta("Sucesso",
                    "Veículo devolvido!\n\n" +
                            "Valor Final Calculado: R$ " + valorCobrado); // Mostra o valor no alerta

            carregarLocacoes();
            lblResumo.setText("Resumo: Selecione uma locação...");

        } catch (SQLException e) {
            mostrarAlerta("Erro", "Falha na devolução: " + e.getMessage());
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