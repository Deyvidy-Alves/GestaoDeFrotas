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

    // metodos que disparam a troca de tela para cada funcionalidade
    @FXML protected void irParaVeiculos(ActionEvent event) { trocarTela(event, "cadastro-veiculo-view.fxml", "novo veículo"); }
    @FXML protected void irParaListaVeiculos(ActionEvent event) { trocarTela(event, "lista-veiculos-view.fxml", "gerenciar frota"); }
    @FXML protected void irParaClientes(ActionEvent event) { trocarTela(event, "cadastro-cliente-view.fxml", "novo cliente"); }
    @FXML protected void irParaListaClientes(ActionEvent event) { trocarTela(event, "lista-clientes-view.fxml", "gerenciar clientes"); }
    @FXML protected void irParaLocacao(ActionEvent event) { trocarTela(event, "locacao-view.fxml", "nova locação"); }
    @FXML protected void onAbrirDevolucao(ActionEvent event) { trocarTela(event, "devolucao-view.fxml", "devolução de veículo"); }
    @FXML protected void irParaRelatorios(ActionEvent event) { trocarTela(event, "relatorio-view.fxml", "relatórios financeiros"); }
    @FXML protected void onAbrirManutencao(ActionEvent event) { trocarTela(event, "manutencao-view.fxml", "oficina - manutenção"); }

    // mata o processo do java completamente
    @FXML
    protected void sair() {
        System.exit(0);
    }

    // engrenagem mestre que resolve os erros de localizacao e tamanho
    private void trocarTela(ActionEvent event, String nomeArquivoFxml, String titulo) {
        try {
            // tenta achar o arquivo fxml na pasta de recursos
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(nomeArquivoFxml));

            // se nao achar pelo caminho relativo, tenta pelo caminho absoluto do projeto
            if (fxmlLoader.getLocation() == null) {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/gestaodefrotas/" + nomeArquivoFxml));
            }

            // se o arquivo sumiu ou o nome ta errado, cancela a operacao
            if (fxmlLoader.getLocation() == null) {
                System.err.println("arquivo não encontrado: " + nomeArquivoFxml);
                return;
            }

            // carrega o desenho da nova tela
            Scene scene = new Scene(fxmlLoader.load());
            // pega a moldura da janela atual (stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // desliga o maximizado do windows antes de trocar a cena
            stage.setMaximized(false);

            // troca o conteudo da janela
            stage.setScene(scene);
            stage.setTitle(titulo);

            // força a largura e altura desejada
            stage.setWidth(850);
            stage.setHeight(650);

            // centraliza a janela no monitor para nao ficar "perdida" no canto
            stage.centerOnScreen();

            // trava o tamanho para o usuario nao baguncar o layout
            stage.setResizable(false);

            stage.show();

        } catch (IOException e) {
            // se o fxml tiver algum erro de sintaxe, o erro aparece no console do intellij
            System.err.println("erro ao carregar o arquivo fxml: " + nomeArquivoFxml);
            e.printStackTrace();
        }
    }
}