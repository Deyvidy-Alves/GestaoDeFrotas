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
    protected void irParaListaVeiculos(ActionEvent event) {
        // tela que arrumamos hj mais cedo
        trocarTela(event, "lista-veiculos-view.fxml", "gerenciar frota");
    }

    @FXML
    protected void irParaClientes(ActionEvent event) {
        trocarTela(event, "cadastro-cliente-view.fxml", "novo cliente");
    }

    @FXML
    protected void irParaListaClientes(ActionEvent event) {
        // AQUI ESTAVA O ERRO DE DUPLICACAO! Agora existe apenas um, usando a engrenagem correta.
        trocarTela(event, "lista-clientes-view.fxml", "gerenciar clientes");
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
    protected void irParaRelatorios(ActionEvent event) {
        // chama o relatorio de grana
        trocarTela(event, "relatorio-view.fxml", "relatórios financeiros");
    }

    @FXML
    protected void onAbrirManutencao(ActionEvent event) {
        // chama a tela da oficina
        trocarTela(event, "manutencao-view.fxml", "oficina - manutenção");
    }

    // esse é o botao vermelho de fechar o app
    @FXML
    protected void sair() {
        // fecha o java completamente com codigo zero (zero quer dizer fechado sem erros graves)
        System.exit(0);
    }

    // essa aqui é a engrenagem mestre que criamos hoje cedo pra resolver os seus erros de 'location is not set'
    private void trocarTela(ActionEvent event, String nomeArquivoFxml, String titulo) {
        try {
            // tenta achar o arquivo relativo a nossa classe rodando
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(nomeArquivoFxml));

            // se nao achou, tenta o caminho absoluto
            if (fxmlLoader.getLocation() == null) {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/gestaodefrotas/" + nomeArquivoFxml));
            }

            // se continuar nulo, cancela pra nao explodir o java
            if (fxmlLoader.getLocation() == null) {
                System.err.println("arquivo não encontrado: " + nomeArquivoFxml);
                return;
            }

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle(titulo);
            stage.setScene(scene);

            // a magica que impede a tela de ficar pulando e mudando de tamanho
            // trava a largura e a altura sempre no mesmo padrao
            stage.setWidth(850);
            stage.setHeight(650);
            // impede o usuario de maximizar e baguncar o layout da tela
            stage.setResizable(false);

            stage.show();

        } catch (IOException e) {
            System.err.println("erro ao carregar o arquivo fxml: " + nomeArquivoFxml);
            e.printStackTrace();
        }
    }
}