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

        LocalDate hoje = LocalDate.now();
        LocalDate dataPrevista = loc.getDataDevolucaoPrevista();

        // 1. Proteção contra data futura (Viagem no tempo)
        if (hoje.isBefore(loc.getDataRetirada())) {
            mostrarAlerta("Erro", "Impossível devolver! A data de retirada é no futuro (" + loc.getDataRetirada() + ").");
            return;
        }

        try {
            double valorDiaria = loc.getVeiculo().getValorDiaria();

            // --- CÁLCULOS DETALHADOS ---

            // A. Dias Realmente Usados (Cobrança Normal)
            long diasUsados = ChronoUnit.DAYS.between(loc.getDataRetirada(), hoje);
            if (diasUsados == 0) diasUsados = 1; // Cobra pelo menos 1 dia se devolver no mesmo dia
            double valorPelosDiasUsados = diasUsados * valorDiaria;

            // B. Verificação de Antecipação ou Atraso
            double multaAtraso = 0.0;
            double taxaRescisaoAntecipada = 0.0;
            long diasRestantes = 0;
            long diasAtraso = 0;

            if (hoje.isBefore(dataPrevista)) {
                // CASO 1: Devolução Antecipada (Aplica regra dos 30%)
                diasRestantes = ChronoUnit.DAYS.between(hoje, dataPrevista);
                double valorDiasRestantes = diasRestantes * valorDiaria;
                taxaRescisaoAntecipada = valorDiasRestantes * 0.30; // 30% sobre o que sobrou
            }
            else if (hoje.isAfter(dataPrevista)) {
                // CASO 2: Devolução com Atraso
                diasAtraso = ChronoUnit.DAYS.between(dataPrevista, hoje);
                multaAtraso = (diasAtraso * valorDiaria) + 50.0; // Diárias extras + Taxa fixa
            }

            // C. Total Final
            double totalFinal = valorPelosDiasUsados + multaAtraso + taxaRescisaoAntecipada;

            // --- ATUALIZAÇÃO NO BANCO ---
            // Aqui estamos usando um "truque": como o DAO espera o objeto Locacao preenchido,
            // poderíamos criar um método específico no DAO para salvar multa separada,
            // mas para simplificar, vamos salvar o valor total calculado aqui.
            new LocacaoDAO().registrarDevolucao(loc);

            // --- MONTAGEM DO RECIBO INTELIGENTE ---
            StringBuilder recibo = new StringBuilder();
            recibo.append("RESUMO DA DEVOLUÇÃO\n");
            recibo.append("--------------------------------\n");
            recibo.append(String.format("Veículo: %s\n", loc.getVeiculo().getModelo()));
            recibo.append(String.format("Cliente: %s\n\n", loc.getCliente().getNome()));

            recibo.append(String.format("(+) Dias Utilizados (%d): R$ %.2f\n", diasUsados, valorPelosDiasUsados));

            if (taxaRescisaoAntecipada > 0) {
                recibo.append(String.format("(+) Taxa Rescisão Antecipada (%d dias não usados): R$ %.2f\n", diasRestantes, taxaRescisaoAntecipada));
                recibo.append("    *Motivo: Devolução antes do prazo contratado (30% sobre restante).\n");
            }

            if (multaAtraso > 0) {
                recibo.append(String.format("(+) Multa por Atraso (%d dias): R$ %.2f\n", diasAtraso, multaAtraso));
            }

            recibo.append("--------------------------------\n");
            recibo.append(String.format("(=) TOTAL A PAGAR: R$ %.2f", totalFinal));


            mostrarAlerta("Sucesso", "Veículo devolvido com sucesso!\n\n" + recibo.toString());

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