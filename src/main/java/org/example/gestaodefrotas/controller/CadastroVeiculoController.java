package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Carro;
import org.example.gestaodefrotas.model.Moto;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;

public class CadastroVeiculoController {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtModelo;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtKm;
    @FXML private TextField txtValor;
    @FXML private TextField txtDetalhe;

    private Veiculo veiculoEmEdicao = null;

    @FXML
    public void initialize() {
        // preenche as opcoes da frota
        cbTipo.getItems().addAll("CARRO", "MOTO");
        cbTipo.setValue("CARRO");
    }

    public void prepararEdicao(Veiculo v) {
        this.veiculoEmEdicao = v;
        if (lblTitulo != null) lblTitulo.setText("editar veículo");

        txtModelo.setText(v.getModelo());
        txtPlaca.setText(v.getPlaca());
        txtKm.setText(String.valueOf(v.getKm()));
        txtValor.setText(String.valueOf(v.getValorDiaria()));

        // se for moto ou carro, ele marca a opcao certa na tela
        if (v instanceof Moto) {
            cbTipo.setValue("MOTO");
            txtDetalhe.setText(String.valueOf(((Moto) v).getCilindradas()));
        } else if (v instanceof Carro) {
            cbTipo.setValue("CARRO");
            txtDetalhe.setText(String.valueOf(((Carro) v).getQuantidadePortas()));
        }

        // trava a troca de tipo na edicao
        cbTipo.setDisable(true);
    }

    // o metodo acionado ao clicar no botao salvar
    @FXML
    protected void onSalvar() {
        try {
            // coleta os textos digitados
            String modelo = txtModelo.getText();
            // o trim() arranca espacos em branco que o usuario pode ter digitado sem querer antes ou depois da placa
            String placa = txtPlaca.getText().trim();
            int km = Integer.parseInt(txtKm.getText());
            double valor = Double.parseDouble(txtValor.getText());
            int detalhe = Integer.parseInt(txtDetalhe.getText());
            String tipo = cbTipo.getValue();

            // validacao de campos vazios para nao dar erro no banco
            if (modelo.isEmpty() || placa.isEmpty()) {
                mostrarAlerta("atenção", "modelo e placa são obrigatórios!");
                return;
            }

            VeiculoDAO dao = new VeiculoDAO();

            // regra de negocio: tratamento de placas clonadas/duplicadas
            // se for cadastro novo, o id e -1. se for edicao, pega o id verdadeiro do carro
            int idAtual = (veiculoEmEdicao == null) ? -1 : veiculoEmEdicao.getId();

            // o controller pergunta pro dao se a placa existe. se retornar true, bloqueia e cancela o salvamento
            if (dao.placaExiste(placa, idAtual)) {
                mostrarAlerta("erro de validação", "já existe outro veículo cadastrado com a placa " + placa + "!");
                return; // o return para a execucao do metodo aqui e nao deixa salvar
            }

            if (veiculoEmEdicao == null) {
                // aplicacao pratica da heranca e poo
                Veiculo novoVeiculo;
                if ("MOTO".equals(tipo)) {
                    // cria um objeto moto na memoria
                    novoVeiculo = new Moto(modelo, placa, km, valor, detalhe);
                } else {
                    // cria um objeto carro na memoria
                    novoVeiculo = new Carro(modelo, placa, km, valor, detalhe);
                }

                dao.salvar(novoVeiculo);
                mostrarAlerta("sucesso", tipo.toLowerCase() + " cadastrado com sucesso!");

            } else {
                veiculoEmEdicao.setModelo(modelo);
                veiculoEmEdicao.setPlaca(placa);
                veiculoEmEdicao.setKm(km);
                veiculoEmEdicao.setValorDiaria(valor);

                // lanca o detalhe especifico dependendo da classe instanciada
                if (veiculoEmEdicao instanceof Moto) {
                    ((Moto) veiculoEmEdicao).setCilindradas(detalhe);
                } else if (veiculoEmEdicao instanceof Carro) {
                    ((Carro) veiculoEmEdicao).setQuantidadePortas(detalhe);
                }

                dao.atualizar(veiculoEmEdicao);
                mostrarAlerta("sucesso", "veículo atualizado!");
            }

            limparCampos();
            veiculoEmEdicao = null;
            if (lblTitulo != null) lblTitulo.setText("novo veículo");
            cbTipo.setDisable(false);

        } catch (NumberFormatException e) {
            // tratamento de erro: se o usuario digitar letra onde e numero, o java avisa em vez de quebrar
            mostrarAlerta("erro", "km, valor e detalhe (portas/cilindradas) devem ser números válidos!");
        } catch (Exception e) {
            mostrarAlerta("erro", "falha na operação: " + e.getMessage());
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

    private void limparCampos() {
        txtModelo.clear();
        txtPlaca.clear();
        txtKm.clear();
        txtValor.clear();
        txtDetalhe.clear();
        cbTipo.setValue("CARRO");
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}