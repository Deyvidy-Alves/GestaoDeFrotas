// define o pacote onde o arquivo esta guardado
package org.example.gestaodefrotas.controller;

// importacoes necessarias para a interface grafica funcionar
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
// importacoes do nosso proprio sistema
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.model.Cliente;

// importacao padrao de excecoes de entrada e saida
import java.io.IOException;

// classe responsavel por dar vida a tela de cadastro de cliente
public class CadastroClienteController {
    // mapeia a caixa de texto do nome que esta desenhada no fxml
    @FXML private TextField txtNome;
    // mapeia a caixa de texto do cpf
    @FXML private TextField txtCpf;
    // mapeia a caixa de texto da cnh
    @FXML private TextField txtCnh;
    // mapeia o calendario visual para escolher a data de validade da cnh
    @FXML private DatePicker dtValidade;
    // mapeia a caixa de texto do telefone
    @FXML private TextField txtTelefone;

    // metodo que e ativado quando o usuario clica no botao "salvar cliente"
    @FXML
    protected void onSalvar() {
        try {
            // le todos os textos digitados na tela e ja cria um objeto cliente novo na memoria
            Cliente c = new Cliente(
                    txtNome.getText(),
                    txtCpf.getText(),
                    txtCnh.getText(),
                    dtValidade.getValue(), // getValue pega a data formato localdate
                    txtTelefone.getText()
            );

            // chama a classe dao, abre a conexao com banco, grava o cliente e fecha
            new ClienteDAO().salvar(c);

            // avisa que o cadastro deu certo
            mostrarAlerta("sucesso", "cliente cadastrado!");
            // apaga os textos da tela para poder cadastrar o proximo
            limpar();
        } catch (Exception e) {
            // se der algum erro de banco (ex: cpf repetido), avisa o usuario
            mostrarAlerta("erro", "erro ao salvar: " + e.getMessage());
        }
    }

    // metodo do botao "voltar" para retornar a tela inicial
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // carrega o desenho da tela do menu
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // descobre qual janela o usuario esta usando
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // altera o titulo da janela no topo
            stage.setTitle("gestão de frota");
            // coloca a cena do menu dentro da janela
            stage.setScene(scene);
            // mostra na tela
            stage.show();
        } catch (IOException e) {
            // cospe o erro no terminal se nao achar o arquivo do menu
            e.printStackTrace();
        }
    }

    // metodo ajudante para mostrar avisos pop-up na tela
    private void mostrarAlerta(String titulo, String msg) {
        // cria um alerta do tipo informacao
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        // trava o uso do programa ate o usuario fechar o aviso
        alert.showAndWait();
    }

    // metodo que esvazia todas as caixas de texto apos um salvamento
    private void limpar() {
        txtNome.clear();
        txtCpf.clear();
        txtCnh.clear();
        txtTelefone.clear();
        // zera a data
        dtValidade.setValue(null);
    }
}