// pacote
package org.example.gestaodefrotas.controller;

// importacoes incluindo as das tabelas visuais e do observador (observablelist)
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ConexaoDB;
import org.example.gestaodefrotas.model.Veiculo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// classe que administra a grade (tabela) que lista toda a frota
public class ListaVeiculosController {

    // amarra o componente gigantao da tabela da tela
    @FXML private TableView<Veiculo> tabelaVeiculos;
    // amarra a coluna do id
    @FXML private TableColumn<Veiculo, Integer> colId;
    // amarra a coluna do modelo
    @FXML private TableColumn<Veiculo, String> colModelo;
    // amarra a placa
    @FXML private TableColumn<Veiculo, String> colPlaca;
    // amarra o km atual do carro
    @FXML private TableColumn<Veiculo, Integer> colKm;
    // amarra a situacao dele
    @FXML private TableColumn<Veiculo, String> colStatus;
    // amarra o custo da locacao
    @FXML private TableColumn<Veiculo, Double> colValor;

    // metodo automatico de preparacao
    @FXML
    public void initialize() {
        // "propertyvaluefactory" ensina o javafx a pegar o getter la da classe veiculo e preencher as celulas automatico
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colKm.setCellValueFactory(new PropertyValueFactory<>("km"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorDiaria"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // assim que mapeou, carrega os dados do banco e injeta na grade
        carregarTabela();
    }

    // preenche os veiculos
    private void carregarTabela() {
        // o observablelist avisa a tabela sempre que um dado entra ou sai, renderizando a tela ao vivo
        ObservableList<Veiculo> veiculos = FXCollections.observableArrayList();

        // sql basicao pra trazer a lista de frota inteira de uma vez
        String sql = "SELECT * FROM veiculos";

        // abre o mysql e executa (lembra do module-info abrindo o acesso pra ler esses dados?)
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // pra cada carro achado no select
            while (rs.next()) {
                // reconstroi no java
                Veiculo v = new Veiculo(
                        rs.getString("modelo"),
                        rs.getString("placa"),
                        rs.getInt("km_atual"),
                        rs.getDouble("valor_diaria")
                );
                v.setId(rs.getInt("id"));
                v.setStatus(rs.getString("status"));

                // joga o carro na lista que a tela entende
                veiculos.add(v);
            }
            // finalmente poe a lista cheia dentro do componente grafico da tela fxml
            tabelaVeiculos.setItems(veiculos);

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao carregar frota: " + e.getMessage());
        }
    }

    // botao que pega o carro que vc clicou e abre a tela de edicao
    @FXML
    protected void onEditar(ActionEvent event) {
        // getselectionmodel sabe em qual quadradinho o seu mouse clicou
        Veiculo selecionado = tabelaVeiculos.getSelectionModel().getSelectedItem();

        // se clicou no botao de editar sem clicar em nenhum carro da lista antes
        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um veículo na tabela primeiro!");
            return;
        }

        try {
            // prepara para trocar para a tela de cadastro
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("cadastro-veiculo-view.fxml"));
            Parent root = loader.load();

            // pega a sala de controle da proxima tela (cadastroveiculocontroller) antes dela abrir
            CadastroVeiculoController controller = loader.getController();

            // injeta o carro selecionado la dentro, ativando as modificacoes de tela
            controller.prepararEdicao(selecionado);

            // e so entao carrega a proxima cena visualmente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // acao de apagar o carro da frota
    @FXML
    protected void onExcluir(ActionEvent event) {
        // descobre quem tu quer apagar
        Veiculo selecionado = tabelaVeiculos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um veículo para excluir!");
            return;
        }

        // essa e a trava intencional para evitar de deletar o passado. o banco mysql ia jogar erro de foreign key se excluissemos um carro que ja tinha sido alugado. entao a gente proibe e manda ele so inativar se quiser.
        mostrarAlerta("aviso", "a exclusão direta está desativada para não quebrar o histórico de locações. edite os dados do veículo se necessário.");
    }

    // transita de volta
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

    // gerador de informacoes
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}