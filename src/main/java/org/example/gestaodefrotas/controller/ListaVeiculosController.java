// pacote padrao
package org.example.gestaodefrotas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ConexaoDB;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.model.Veiculo;
import org.example.gestaodefrotas.model.Carro;
import org.example.gestaodefrotas.model.Moto;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ListaVeiculosController {

    @FXML private TableView<Veiculo> tabelaVeiculos;
    @FXML private TableColumn<Veiculo, Integer> colId;
    @FXML private TableColumn<Veiculo, String> colModelo;
    @FXML private TableColumn<Veiculo, String> colPlaca;
    @FXML private TableColumn<Veiculo, Integer> colKm;
    @FXML private TableColumn<Veiculo, String> colStatus;
    @FXML private TableColumn<Veiculo, Double> colValor;

    // mapeia a nova caixa de filtro
    @FXML private ComboBox<String> cbFiltroTipo;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colKm.setCellValueFactory(new PropertyValueFactory<>("km"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorDiaria"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // configura o filtro se ele existir na tela
        if (cbFiltroTipo != null) {
            cbFiltroTipo.getItems().addAll("TODOS", "CARROS", "MOTOS");
            cbFiltroTipo.setValue("TODOS");

            // ensina o filtro a recarregar a tabela automaticamente quando voce muda a opcao
            cbFiltroTipo.setOnAction(event -> carregarTabela());
        }

        carregarTabela();
    }

    private void carregarTabela() {
        ObservableList<Veiculo> veiculos = FXCollections.observableArrayList();

        // a instrucao basica que ignora os veiculos inativados pelo botao de excluir
        String sql = "SELECT * FROM veiculos WHERE status != 'INATIVO'";

        // aplica o separador se o usuario tiver escolhido no combobox
        if (cbFiltroTipo != null) {
            if ("CARROS".equals(cbFiltroTipo.getValue())) {
                sql += " AND tipo = 'CARRO'";
            } else if ("MOTOS".equals(cbFiltroTipo.getValue())) {
                sql += " AND tipo = 'MOTO'";
            }
        }

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                Veiculo v;

                if ("MOTO".equals(tipo)) {
                    v = new Moto(
                            rs.getString("modelo"),
                            rs.getString("placa"),
                            rs.getInt("km_atual"),
                            rs.getDouble("valor_diaria"),
                            rs.getInt("cilindradas")
                    );
                } else {
                    v = new Carro(
                            rs.getString("modelo"),
                            rs.getString("placa"),
                            rs.getInt("km_atual"),
                            rs.getDouble("valor_diaria"),
                            rs.getInt("portas")
                    );
                }

                v.setId(rs.getInt("id"));
                v.setStatus(rs.getString("status"));

                veiculos.add(v);
            }
            tabelaVeiculos.setItems(veiculos);

        } catch (Exception e) {
            mostrarAlerta("erro", "falha ao carregar frota: " + e.getMessage());
        }
    }

    @FXML
    protected void onEditar(ActionEvent event) {
        Veiculo selecionado = tabelaVeiculos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um veículo na tabela primeiro!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("cadastro-veiculo-view.fxml"));
            Parent root = loader.load();

            CadastroVeiculoController controller = loader.getController();
            controller.prepararEdicao(selecionado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // o botao excluir agora faz inativacao
    @FXML
    protected void onExcluir(ActionEvent event) {
        Veiculo selecionado = tabelaVeiculos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta("atenção", "selecione um veículo para excluir!");
            return;
        }

        // pede confirmacao para nao ter exclusoes acidentais
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("confirmar exclusão");
        confirmacao.setHeaderText("excluir " + selecionado.getModelo() + "?");
        confirmacao.setContentText("o veículo será inativado e sumirá das telas, mas os relatórios antigos continuarão intactos.");

        if (confirmacao.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                // aciona o dao para mudar o status para inativo
                new VeiculoDAO().inativar(selecionado.getId());
                mostrarAlerta("sucesso", "veículo removido com sucesso!");
                // recarrega a tabela (ele vai sumir porque o sql so busca != 'inativo')
                carregarTabela();
            } catch (Exception e) {
                mostrarAlerta("erro", "falha ao excluir: " + e.getMessage());
            }
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

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}