package org.example.gestaodefrotas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import java.io.IOException;

// a rotatoria do seu programa, que liga uma tela a outra
public class MenuController {

    // todos esses metodos com @fxml sao botoes da interface que chamam o metodo centralizado passando 3 coisas:
    // o evento de clique, o nome do arquivo fxml de destino e o titulo que a janela deve ganhar

    @FXML
    protected void irParaVeiculos(ActionEvent event) {
        trocarTela(event, "cadastro-veiculo-view.fxml", "novo veículo");
    }

    @FXML
    protected void irParaClientes(ActionEvent event) {
        trocarTela(event, "cadastro-cliente-view.fxml", "novo cliente");
    }

    @FXML
    protected void irParaLocacao(ActionEvent event) {
        trocarTela(event, "locacao-view.fxml", "nova locação");
    }

    @FXML
    protected void onAbrirDevolucao(ActionEvent event) {
        trocarTela(event, "devolucao-view.fxml", "devolução de veículo");
    }

    @FXML
    protected void onAbrirManutencao(ActionEvent event) {
        // chama a tela da oficina
        trocarTela(event, "manutencao-view.fxml", "oficina - manutenção");
    }

    @FXML
    protected void irParaRelatorios(ActionEvent event) {
        // chama o relatorio de grana
        trocarTela(event, "relatorio-view.fxml", "relatórios financeiros");
    }

    @FXML
    protected void irParaListaVeiculos(ActionEvent event) {
        // tela que arrumamos hj mais cedo
        trocarTela(event, "lista-veiculos-view.fxml", "gerenciar frota");
    }

    // esse é o botao vermelho de fechar o app
    @FXML
    protected void sair() {
        // fecha o java completamente com codigo zero (zero quer dizer fechado sem erros graves)
        System.exit(0);
    }

    @FXML
    protected void irParaListaClientes(ActionEvent event) {
        trocarTela(event, "lista-clientes-view.fxml", "gerenciar clientes");
    }

    // essa aqui é a engrenagem mestre que criamos hoje cedo pra resolver os seus erros de 'location is not set'
    private void trocarTela(ActionEvent event, String nomeArquivoFxml, String titulo) {
        try {
            // tenta achar o arquivo relativo a nossa classe rodando
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(nomeArquivoFxml));

            // se nao achou (deu nulo), tenta passar o endereco completo da raiz dos recursos ate o arquivo
            if (fxmlLoader.getLocation() == null) {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/gestaodefrotas/" + nomeArquivoFxml));
            }

            // se continuou nulo, o arquivo fisicamente sumiu ou o nome ta digitado errado
            if (fxmlLoader.getLocation() == null) {
                System.err.println("❌ arquivo não encontrado: " + nomeArquivoFxml);
                return; // cancela a troca de tela pra nao explodir o java
            }

            // carrega o visual
            Scene scene = new Scene(fxmlLoader.load());

            // pega a moldura principal do windows
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // arruma o nome da janela e joga a tela la dentro
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // erro ao interpretar o desenho (fxml)
            System.err.println("❌ erro ao carregar o arquivo fxml: " + nomeArquivoFxml);
            e.printStackTrace();
        }
    }

}