// pacote padrao
package org.example.gestaodefrotas.controller;

// importacoes necessarias
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.LocacaoDAO;
import org.example.gestaodefrotas.dao.VistoriaDAO;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Vistoria;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DevolucaoController {

    @FXML private ComboBox<Locacao> cbLocacoes;
    @FXML private Label lblKmRetirada;
    @FXML private TextField txtKmDevolucao;
    @FXML private ComboBox<String> cbCombustivelRetorno;
    @FXML private TextField txtObsRetorno;

    @FXML
    public void initialize() {
        carregarLocacoes();
        cbCombustivelRetorno.getItems().addAll("cheio", "3/4", "meio tanque (1/2)", "1/4", "reserva");
        cbLocacoes.setOnAction(event -> atualizarResumo());
    }

    private void carregarLocacoes() {
        try {
            cbLocacoes.getItems().clear();
            cbLocacoes.getItems().addAll(new LocacaoDAO().listarEmAberto());
        } catch (SQLException e) {
            mostrarAlerta("erro", "erro ao carregar locações: " + e.getMessage());
        }
    }

    private void atualizarResumo() {
        Locacao loc = cbLocacoes.getValue();
        if (loc != null) {
            lblKmRetirada.setText("km na retirada: " + loc.getVeiculo().getKm());
        } else {
            lblKmRetirada.setText("km na retirada: --");
        }
    }

    @FXML
    protected void onConfirmar() {
        Locacao loc = cbLocacoes.getValue();

        if (loc == null || cbCombustivelRetorno.getValue() == null || txtKmDevolucao.getText().isEmpty()) {
            mostrarAlerta("atenção", "preencha todos os campos!");
            return;
        }

        int kmDevolucao = Integer.parseInt(txtKmDevolucao.getText());
        if (kmDevolucao < loc.getVeiculo().getKm()) {
            mostrarAlerta("erro", "km atual não pode ser menor que o de retirada!");
            return;
        }

        try {
            LocalDate hoje = LocalDate.now();
            LocalDate retirada = loc.getDataRetirada();
            LocalDate prevista = loc.getDataDevolucaoPrevista();
            double valorDiaria = loc.getVeiculo().getValorDiaria();

            // descobre quantos dias ele prometeu ficar
            long diasContratados = ChronoUnit.DAYS.between(retirada, prevista);
            if (diasContratados <= 0) diasContratados = 1;

            // descobre quantos dias ele realmente ficou
            long diasReais = ChronoUnit.DAYS.between(retirada, hoje);
            if (diasReais <= 0) diasReais = 1;

            double valorBase = diasReais * valorDiaria;
            double multaAtraso = 0;
            double multaAntecipada = 0;
            double taxaCombustivel = 0;
            double taxaSinistro = 0;

            StringBuilder extrato = new StringBuilder();
            extrato.append("--- extrato de devolução atualizado ---\n");
            extrato.append(String.format("cliente: %s\n", loc.getCliente().getNome()));
            extrato.append(String.format("veículo: %s\n", loc.getVeiculo().getModelo()));
            extrato.append(String.format("dias utilizados: %d\n", diasReais));

            // o texto novo que prova que a versao nova compilou!
            extrato.append(String.format("💰 valor diárias usadas: r$ %.2f\n", valorBase));

            // regra 1: quebra de contrato (devolveu antes). multa de 30%
            if (hoje.isBefore(prevista)) {
                long diasNaoUsados = diasContratados - diasReais;
                multaAntecipada = (diasNaoUsados * valorDiaria) * 0.30;
                extrato.append(String.format("⏳ multa dev. antecipada (30%%): r$ %.2f\n", multaAntecipada));
            }
            // regra 2: atraso. multa de 50%
            else if (hoje.isAfter(prevista)) {
                long diasAtraso = diasReais - diasContratados;
                multaAtraso = (diasAtraso * valorDiaria) * 0.50;
                extrato.append(String.format("⚠️ multa por atraso (50%%): r$ %.2f\n", multaAtraso));
            }

            // regra 3: combustivel
            String combustivelSaida = new VistoriaDAO().buscarCombustivelRetirada(loc.getId());
            String combustivelVolta = cbCombustivelRetorno.getValue();

            int nivelSaida = converterNivelCombustivel(combustivelSaida);
            int nivelVolta = converterNivelCombustivel(combustivelVolta);

            // se voltou com menos do que saiu
            if (nivelVolta < nivelSaida) {
                int diferenca = nivelSaida - nivelVolta;

                // aqui esta o polimorfismo puro agindo!
                // o java pergunta pro objeto qual e a taxa dele, sem precisar de if ou instanceof
                double valorPorQuarto = loc.getVeiculo().getTaxaReabastecimento();

                taxaCombustivel = diferenca * valorPorQuarto;
                extrato.append(String.format("⛽ reabastecimento (%s -> %s): r$ %.2f\n", combustivelSaida, combustivelVolta, taxaCombustivel));
            }

            // regra 4: sinistro/avaria
            if (!txtObsRetorno.getText().trim().isEmpty()) {
                taxaSinistro = 150.00;
                extrato.append(String.format("🚗 taxa avaria/limpeza: r$ %.2f\n", taxaSinistro));
            }

            double totalFinal = valorBase + multaAntecipada + multaAtraso + taxaCombustivel + taxaSinistro;
            extrato.append("----------------------------\n");
            extrato.append(String.format("total final a pagar: r$ %.2f", totalFinal));

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("fechamento de conta");
            confirmacao.setHeaderText("confirme os valores com o cliente:");
            confirmacao.setContentText(extrato.toString());

            if (confirmacao.showAndWait().get() == ButtonType.OK) {
                loc.getVeiculo().setKm(kmDevolucao);
                loc.setValorTotal(totalFinal);

                Vistoria vis = new Vistoria();
                vis.setLocacao(loc);
                vis.setTipo("DEVOLUCAO");
                vis.setNivelCombustivel(combustivelVolta);
                vis.setObservacoes(txtObsRetorno.getText());
                vis.setDataVistoria(hoje);

                new VistoriaDAO().salvar(vis);
                new LocacaoDAO().registrarDevolucao(loc);

                mostrarAlerta("sucesso", "devolução registrada com sucesso!");
                limparFormulario();
                carregarLocacoes();
            }

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao processar: " + e.getMessage());
        }
    }

    // converte as palavras do combobox em numeros para fazer matematica de subtracao
    private int converterNivelCombustivel(String nivel) {
        if (nivel == null) return 4;
        switch (nivel.toLowerCase()) {
            case "cheio": return 4;
            case "3/4": return 3;
            case "meio tanque (1/2)": return 2;
            case "1/4": return 1;
            case "reserva": return 0;
            default: return 4;
        }
    }

    private void limparFormulario() {
        txtKmDevolucao.clear();
        txtObsRetorno.clear();
        cbCombustivelRetorno.setValue(null);
        cbLocacoes.setValue(null);
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