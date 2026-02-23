// pacote padrao dos controladores
package org.example.gestaodefrotas.controller;

// importacoes do javafx
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
// importacoes do nosso sistema
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;

// classe que da vida a tela de cadastro e edicao de veiculos
public class CadastroVeiculoController {

    // titulo da tela ("novo veiculo" ou "editar veiculo")
    @FXML private Label lblTitulo;
    // caixas de digitacao
    @FXML private TextField txtModelo;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtKm;
    @FXML private TextField txtValor;

    // guarda o veiculo se a tela for aberta pelo botao de editar. se for cadastro novo, comeca vazio (null)
    private Veiculo veiculoEmEdicao = null;

    // esse metodo e chamado de fora (pela listaveiculoscontroller) quando o usuario decide editar um carro
    public void prepararEdicao(Veiculo v) {
        // guarda o carro escolhido na memoria
        this.veiculoEmEdicao = v;
        // muda o titulo da tela para o usuario saber que nao e um carro novo
        if (lblTitulo != null) lblTitulo.setText("editar veículo");
        // preenche as caixas de texto com as informacoes que o carro ja tem
        txtModelo.setText(v.getModelo());
        txtPlaca.setText(v.getPlaca());
        txtKm.setText(String.valueOf(v.getKm())); // string.valueof transforma numero em texto
        txtValor.setText(String.valueOf(v.getValorDiaria()));
    }

    // acao do botao verde para confirmar
    @FXML
    protected void onSalvar() {
        try {
            // le tudo que o usuario digitou (ou alterou) nas caixas de texto
            String modelo = txtModelo.getText();
            String placa = txtPlaca.getText();
            // converte os textos de km e valor de volta para numero
            int km = Integer.parseInt(txtKm.getText());
            double valor = Double.parseDouble(txtValor.getText());

            // prepara o meio de campo com o banco de dados
            VeiculoDAO dao = new VeiculoDAO();

            // verifica se e um cadastro novo ou uma edicao
            if (veiculoEmEdicao == null) {
                // se for novo, constroi o carro do zero e usa a funcao salvar
                Veiculo novoCarro = new Veiculo(modelo, placa, km, valor);
                dao.salvar(novoCarro);
                mostrarAlerta("sucesso", "veículo cadastrado!");
            } else {
                // se for edicao, pega o carro que ja existia na memoria e apenas atualiza as pecas dele
                veiculoEmEdicao.setModelo(modelo);
                veiculoEmEdicao.setPlaca(placa);
                veiculoEmEdicao.setKm(km);
                veiculoEmEdicao.setValorDiaria(valor);

                // usa o comando de atualizar (update) la no mysql
                dao.atualizar(veiculoEmEdicao);
                mostrarAlerta("sucesso", "veículo atualizado!");
            }

            // depois de qualquer um dos dois (salvar ou atualizar), limpa tudo pra deixar a tela pronta de novo
            limparCampos();
            veiculoEmEdicao = null;
            lblTitulo.setText("novo veículo");

        } catch (NumberFormatException e) {
            // captura o erro caso o usuario digite "cem" ao inves de "100" nos campos de numero
            mostrarAlerta("erro", "km e valor devem ser números válidos!");
        } catch (Exception e) {
            // captura erro de banco de dados
            mostrarAlerta("erro", "falha na operação: " + e.getMessage());
        }
    }

    // volta pro menu
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

    // limpa os textos escritos
    private void limparCampos() {
        txtModelo.clear();
        txtPlaca.clear();
        txtKm.clear();
        txtValor.clear();
    }

    // lanca aviso na tela
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}