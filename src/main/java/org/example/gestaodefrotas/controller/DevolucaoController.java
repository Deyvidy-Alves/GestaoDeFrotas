package org.example.gestaodefrotas.controller;

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

    // ligacoes com a interface grafica
    @FXML private ComboBox<Locacao> cbLocacoes;
    @FXML private Label lblKmRetirada;
    @FXML private TextField txtKmDevolucao;
    @FXML private ComboBox<String> cbCombustivelRetorno;
    @FXML private TextField txtObsRetorno;

    // inicia ao abrir a tela
    @FXML
    public void initialize() {
        carregarLocacoes();
        cbCombustivelRetorno.getItems().addAll("cheio", "3/4", "meio tanque (1/2)", "1/4", "reserva");
        cbLocacoes.setOnAction(event -> atualizarResumo());
    }

    // busca os contratos no banco que ainda nao foram devolvidos
    private void carregarLocacoes() {
        try {
            cbLocacoes.getItems().clear();
            cbLocacoes.getItems().addAll(new LocacaoDAO().listarEmAberto());
        } catch (SQLException e) {
            mostrarAlerta("erro", "erro ao carregar locações: " + e.getMessage());
        }
    }

    // mostra a km original pro atendente comparar
    private void atualizarResumo() {
        Locacao loc = cbLocacoes.getValue();
        if (loc != null) {
            lblKmRetirada.setText("km na retirada: " + loc.getVeiculo().getKm());
        } else {
            lblKmRetirada.setText("km na retirada: --");
        }
    }

    // botao gigante de confirmar a devolucao e gerar o extrato
    @FXML
    protected void onConfirmar() {
        Locacao loc = cbLocacoes.getValue();

        // protecao contra campos vazios
        if (loc == null || cbCombustivelRetorno.getValue() == null || txtKmDevolucao.getText().isEmpty()) {
            mostrarAlerta("atenção", "preencha todos os campos!");
            return;
        }

        // protecao contra voltar hodometro pra tras
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

            // calculo base das diarias (se der zero dias, arredonda pra 1 diaria minima)
            long diasContratados = ChronoUnit.DAYS.between(retirada, prevista);
            if (diasContratados <= 0) diasContratados = 1;

            long diasReais = ChronoUnit.DAYS.between(retirada, hoje);
            if (diasReais <= 0) diasReais = 1;

            double valorBase = diasReais * valorDiaria;
            double multaAtraso = 0;
            double multaAntecipada = 0;
            double taxaCombustivel = 0;
            double descontoCombustivel = 0; // a variavel nova que vai dar dinheiro de volta pro cliente
            double taxaSinistro = 0;

            // montagem do cupom fiscal da tela
            StringBuilder extrato = new StringBuilder();
            extrato.append("--- extrato de devolução atualizado ---\n");
            extrato.append(String.format("cliente: %s\n", loc.getCliente().getNome()));
            extrato.append(String.format("veículo: %s\n", loc.getVeiculo().getModelo()));
            extrato.append(String.format("dias utilizados: %d\n", diasReais));

            // o R$ maiusculo
            extrato.append(String.format("💰 valor diárias usadas: R$ %.2f\n", valorBase));

            // regra de negocio: quebra de contrato (devolveu antes). multa de 30%
            if (hoje.isBefore(prevista)) {
                long diasNaoUsados = ChronoUnit.DAYS.between(hoje, prevista);
                multaAntecipada = (diasNaoUsados * valorDiaria) * 0.30;
                extrato.append(String.format("⏳ multa dev. antecipada (30%%): R$ %.2f\n", multaAntecipada));
            }
            // regra de negocio: atraso. multa de 50%
            else if (hoje.isAfter(prevista)) {
                long diasAtraso = ChronoUnit.DAYS.between(prevista, hoje);
                multaAtraso = (diasAtraso * valorDiaria) * 0.50;
                extrato.append(String.format("⚠️ multa por atraso (50%%): R$ %.2f\n", multaAtraso));
            }

            // busca como o carro saiu no banco de dados e pega como ele voltou da tela
            String combustivelSaida = new VistoriaDAO().buscarCombustivelRetirada(loc.getId());
            String combustivelVolta = cbCombustivelRetorno.getValue();

            int nivelSaida = converterNivelCombustivel(combustivelSaida);
            int nivelVolta = converterNivelCombustivel(combustivelVolta);

            // regra de negocio de combustivel inteligente: cobra ou ressarce
            if (nivelVolta < nivelSaida) {
                // cliente gastou a gasolina da locadora: cobra dele usando o polimorfismo
                int diferenca = nivelSaida - nivelVolta;
                double valorPorQuarto = loc.getVeiculo().getTaxaReabastecimento();
                taxaCombustivel = diferenca * valorPorQuarto;
                extrato.append(String.format("⛽ reabastecimento (%s -> %s): R$ %.2f\n", combustivelSaida, combustivelVolta, taxaCombustivel));
            } else if (nivelVolta > nivelSaida) {
                // a sacada genial: cliente devolveu com mais gasolina do que pegou, ganha desconto!
                int diferenca = nivelVolta - nivelSaida;
                double valorPorQuarto = loc.getVeiculo().getTaxaReabastecimento(); // polimorfismo dinamico (moto 25, carro 100)
                descontoCombustivel = diferenca * valorPorQuarto;
                extrato.append(String.format("🎁 reembolso de combustível (%s -> %s): -R$ %.2f\n", combustivelSaida, combustivelVolta, descontoCombustivel));
            }

            // se tiver texto de avaria, cobra a taxa extra de 150 reais
            if (!txtObsRetorno.getText().trim().isEmpty()) {
                taxaSinistro = 150.00;
                extrato.append(String.format("🚗 taxa avaria/limpeza: R$ %.2f\n", taxaSinistro));
            }

            // junta todo o dinheiro (soma as taxas e multas, e subtrai o desconto do combustivel)
            double totalFinal = valorBase + multaAntecipada + multaAtraso + taxaCombustivel + taxaSinistro - descontoCombustivel;

            // protecao financeira: a locadora abate do valor do aluguel, mas nunca fica devendo dinheiro pro cliente
            if (totalFinal < 0) totalFinal = 0.0;

            extrato.append("----------------------------\n");
            extrato.append(String.format("total final a pagar: R$ %.2f", totalFinal));

            // joga a tela do extrato pro usuario confirmar
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("fechamento de conta");
            confirmacao.setHeaderText("confirme os valores com o cliente:");
            confirmacao.setContentText(extrato.toString());

            // se ele clicar em OK, altera tudo no banco de dados
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

    // converte texto em numero pra facilitar a conta do combustivel
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

    // apaga tudo quando terminar o processo
    private void limparFormulario() {
        txtKmDevolucao.clear();
        txtObsRetorno.clear();
        cbCombustivelRetorno.setValue(null);
        cbLocacoes.setValue(null);
    }

    // botao de retornar ao menu em tela cheia
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // balaozinho padrao de recados
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}