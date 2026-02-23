// pacote padrao
package org.example.gestaodefrotas.controller;

// importacoes visuais e das classes que o devolucaocontroller vai mexer
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

// classe grandona que calcula multas, cobra o cliente e encerra o contrato
public class DevolucaoController {

    // combobox onde aparecem os alugueis ativos
    @FXML private ComboBox<Locacao> cbLocacoes;
    // texto que exibe o resumo financeiro ao lado
    @FXML private Label lblResumo;
    // label que mostra o km que o carro tinha na hora que saiu da loja
    @FXML private Label lblKmRetirada;
    // campo onde o funcionario digita o km que ta no painel agora na volta
    @FXML private TextField txtKmDevolucao;
    // caixa para informar se devolveu de tanque cheio ou reserva
    @FXML private ComboBox<String> cbCombustivelRetorno;
    // observacoes se amassaram ou sujaram o carro
    @FXML private TextField txtObsRetorno;

    // inicializa a tela carregando os contratos abertos no banco de dados
    @FXML
    public void initialize() {
        carregarLocacoes();
        // injeta as opcoes de tanque de combustivel na caixa
        cbCombustivelRetorno.getItems().addAll("cheio", "3/4", "meio tanque (1/2)", "1/4", "reserva");
        // manda o sistema atualizar o km da retirada na tela toda vez que voce clicar num cliente diferente na combobox
        cbLocacoes.setOnAction(event -> atualizarResumo());
    }

    // busca no dao todos os contratos que nao tem data de devolucao registrada
    private void carregarLocacoes() {
        try {
            cbLocacoes.getItems().clear();
            cbLocacoes.getItems().addAll(new LocacaoDAO().listarEmAberto());
        } catch (SQLException e) {
            mostrarAlerta("erro", "erro ao carregar locações: " + e.getMessage());
        }
    }

    // muda o textinho do label baseado na locacao que vc escolheu na lista
    private void atualizarResumo() {
        Locacao loc = cbLocacoes.getValue();
        if (loc != null) {
            lblKmRetirada.setText("km na retirada: " + loc.getVeiculo().getKm());
        } else {
            lblKmRetirada.setText("km na retirada: --");
        }
    }

    // acao de fechar a conta
    @FXML
    protected void onConfirmar() {
        Locacao loc = cbLocacoes.getValue();

        // 1. trava basica: nao avanca se tiver campos essenciais vazios
        if (loc == null || cbCombustivelRetorno.getValue() == null || txtKmDevolucao.getText().isEmpty()) {
            mostrarAlerta("atenção", "preencha todos os campos (locação, combustível e km)!");
            return;
        }

        // trava basica 2: nao deixa o cara botar um km menor do que o carro tinha quando saiu
        int kmDevolucao = Integer.parseInt(txtKmDevolucao.getText());
        if (kmDevolucao < loc.getVeiculo().getKm()) {
            mostrarAlerta("erro", "km atual não pode ser menor que o de retirada!");
            return;
        }

        try {
            // 2. inicia as variaveis de data para a matematica da cobranca
            LocalDate hoje = LocalDate.now();
            LocalDate retirada = loc.getDataRetirada();
            LocalDate prevista = loc.getDataDevolucaoPrevista();
            double valorDiaria = loc.getVeiculo().getValorDiaria();

            // chronounit descobre quantos dias se passaram entre a retirada e hoje
            long diasReais = ChronoUnit.DAYS.between(retirada, hoje);
            // nao alugamos meia diaria. se alugou e devolveu no mesmo dia, paga 1 dia
            if (diasReais <= 0) diasReais = 1;

            // multiplica os dias pelo preco do carro
            double valorBase = diasReais * valorDiaria;
            double multaAtraso = 0;
            double taxaSinistro = 0;

            // 3. montando o "cupom fiscal" para aparecer na tela usando stringbuilder
            StringBuilder extrato = new StringBuilder();
            extrato.append("--- extrato de devolução ---\n");
            extrato.append(String.format("cliente: %s\n", loc.getCliente().getNome()));
            extrato.append(String.format("veículo: %s\n", loc.getVeiculo().getModelo()));
            extrato.append(String.format("dias utilizados: %d\n", diasReais));
            extrato.append(String.format("valor base diárias: r$ %.2f\n", valorBase));

            // regras de negocio: checa se atrasou. se hoje e uma data depois do dia combinado
            if (hoje.isAfter(prevista)) {
                // calcula 20% em cima das diarias como multa
                multaAtraso = valorBase * 0.20;
                extrato.append(String.format("⚠️ multa atraso (20%%): r$ %.2f\n", multaAtraso));
            }

            // regras de negocio: checa avaria. se voce digitou qualquer coisa no campo observacao
            if (!txtObsRetorno.getText().trim().isEmpty()) {
                // cobra 150 reais da franquia/taxa do sinistro
                taxaSinistro = 150.00;
                extrato.append(String.format("🚗 taxa sinistro/avaria: r$ %.2f\n", taxaSinistro));
            }

            // junta as 3 coisas pra dar o preco final que o cliente tem que pagar na maquininha
            double totalFinal = valorBase + multaAtraso + taxaSinistro;
            extrato.append("----------------------------\n");
            extrato.append(String.format("total a pagar: r$ %.2f", totalFinal));

            // 4. pop-up de confirmacao pra ter certeza que ninguem digitou errado
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("fechamento de conta");
            confirmacao.setHeaderText("confirme os valores com o cliente:");
            confirmacao.setContentText(extrato.toString());

            // se apertou "ok"
            if (confirmacao.showAndWait().get() == ButtonType.OK) {
                // muda o km do carro na memoria do java e grava o valor que ele lucrou pro relatorio
                loc.getVeiculo().setKm(kmDevolucao);
                loc.setValorTotal(totalFinal);

                // 5. cria a vistoria do momento de volta
                Vistoria vis = new Vistoria();
                vis.setLocacao(loc);
                vis.setTipo("DEVOLUCAO");
                vis.setNivelCombustivel(cbCombustivelRetorno.getValue());
                vis.setObservacoes(txtObsRetorno.getText());
                vis.setDataVistoria(hoje);

                // joga vistoria no banco
                new VistoriaDAO().salvar(vis);
                // vai no banco e finaliza o contrato da locacao inteira e libera o carro de volta pra vitrine
                new LocacaoDAO().registrarDevolucao(loc);

                mostrarAlerta("sucesso", "devolução realizada! o faturamento foi atualizado.");
                // limpa a tela e refaz a lista das locacoes que ainda faltam
                limparFormulario();
                carregarLocacoes();
            }

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao processar: " + e.getMessage());
        }
    }

    // zera os campos visuais
    private void limparFormulario() {
        txtKmDevolucao.clear();
        txtObsRetorno.clear();
        cbCombustivelRetorno.setValue(null);
        cbLocacoes.setValue(null);
    }

    // muda pra tela de menu
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

    // gera o aviso simples
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}