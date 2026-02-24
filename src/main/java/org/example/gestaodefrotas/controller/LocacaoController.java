package org.example.gestaodefrotas.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.dao.LocacaoDAO;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.dao.VistoriaDAO;
import org.example.gestaodefrotas.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LocacaoController {

    // ligacoes visuais
    @FXML private ComboBox<Cliente> cbClientes;
    @FXML private ComboBox<String> cbFiltroVeiculo;
    @FXML private ComboBox<Veiculo> cbVeiculos;
    @FXML private DatePicker dtRetirada;
    @FXML private DatePicker dtPrevista;
    @FXML private ComboBox<String> cbCombustivel;

    private List<Veiculo> todosVeiculosDisponiveis = new ArrayList<>();

    @FXML
    public void initialize() {
        cbCombustivel.getItems().addAll("cheio", "3/4", "meio tanque (1/2)", "1/4", "reserva");
        cbCombustivel.setValue("cheio");

        cbFiltroVeiculo.getItems().addAll("todos", "carros", "motos");
        cbFiltroVeiculo.setValue("todos");

        carregarClientes();
        carregarVeiculos();

        cbFiltroVeiculo.setOnAction(event -> aplicarFiltroVeiculos());

        // impede que o usuario burle o calendario digitando a data "na mao"
        dtRetirada.setEditable(false);
        dtPrevista.setEditable(false);

        // regra visual para desenhar os quadradinhos dos dias
        Callback<DatePicker, DateCell> bloquearDiasPassados = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                // se o dia for antes de hoje...
                if (item != null && item.isBefore(LocalDate.now())) {
                    // desativa o clique no dia
                    setDisable(true);
                    // pinta o fundo de cinza pro usuario ver que ta bloqueado
                    setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #adb5bd;");
                }
            }
        };

        // injeta a regra visual nos dois calendarios
        dtRetirada.setDayCellFactory(bloquearDiasPassados);
        dtPrevista.setDayCellFactory(bloquearDiasPassados);
    }

    private void carregarClientes() {
        try {
            List<Cliente> clientes = new ClienteDAO().listarTodos();
            cbClientes.setItems(FXCollections.observableArrayList(clientes));
        } catch (SQLException e) {
            mostrarAlerta("erro", "falha ao carregar clientes: " + e.getMessage());
        }
    }

    private void carregarVeiculos() {
        try {
            todosVeiculosDisponiveis = new VeiculoDAO().listarDisponiveis();
            aplicarFiltroVeiculos();
        } catch (SQLException e) {
            mostrarAlerta("erro", "falha ao carregar frota: " + e.getMessage());
        }
    }

    private void aplicarFiltroVeiculos() {
        String filtro = cbFiltroVeiculo.getValue();
        List<Veiculo> filtrados = new ArrayList<>();

        for (Veiculo v : todosVeiculosDisponiveis) {
            if ("todos".equals(filtro)) {
                filtrados.add(v);
            } else if ("carros".equals(filtro) && v instanceof Carro) {
                filtrados.add(v);
            } else if ("motos".equals(filtro) && v instanceof Moto) {
                filtrados.add(v);
            }
        }
        cbVeiculos.setItems(FXCollections.observableArrayList(filtrados));
    }

    // o coracao do contrato
    @FXML
    protected void onSalvarLocacao() {
        Cliente cliente = cbClientes.getValue();
        Veiculo veiculo = cbVeiculos.getValue();
        LocalDate retirada = dtRetirada.getValue();
        LocalDate prevista = dtPrevista.getValue();
        String combustivel = cbCombustivel.getValue();

        // pega o dia exato de hoje para fazer a matematica da trava
        LocalDate hoje = LocalDate.now();

        // campos vazios
        if (cliente == null || veiculo == null || retirada == null || prevista == null || combustivel == null) {
            mostrarAlerta("atenção", "por favor, preencha todos os campos do contrato.");
            return;
        }

        // compara estritamente se a retirada escolhida vem antes do dia atual do seu windows
        if (retirada.isBefore(hoje)) {
            mostrarAlerta("erro de data", "operação bloqueada! é impossível alugar um veículo com data retroativa (no passado).");
            return;
        }

        // nao da pra devolver antes de pegar
        if (prevista.isBefore(retirada)) {
            mostrarAlerta("erro", "a data de devolução não pode ser antes da retirada!");
            return;
        }

        // bloqueia o aluguel se a cnh do cara venceu
        if (cliente.getStatus().contains("INATIVO")) {
            mostrarAlerta("detran bloqueou", "operação cancelada! este cliente está com a cnh vencida ou conta inativa.");
            return;
        }

        try {
            Locacao locacao = new Locacao(veiculo, cliente, retirada, prevista);

            LocacaoDAO locacaoDAO = new LocacaoDAO();
            int idGerado = locacaoDAO.salvarERetornarId(locacao);
            locacao.setId(idGerado);

            Vistoria vistoria = new Vistoria(locacao, "RETIRADA", combustivel, "saída padrão", retirada);
            new VistoriaDAO().salvar(vistoria);

            mostrarAlerta("sucesso", "contrato aberto! o veículo agora está alugado.");
            onVoltar(new ActionEvent(cbClientes, null));

        } catch (Exception e) {
            mostrarAlerta("erro", "falha na abertura do contrato: " + e.getMessage());
        }
    }

    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // localiza o arquivo de design do menu principal
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // identifica a janela atual onde o botao foi clicado
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // desliga o estado maximizado para que a janela nao aumente sozinha ao voltar
            stage.setMaximized(false);

            // gruda a cena do menu na janela
            stage.setScene(scene);

            // estabelece a largura e altura padronizadas do sistema
            stage.setWidth(850);
            stage.setHeight(650);

            // impede o usuario de redimensionar a janela manualmente
            stage.setResizable(false);

            // reposiciona a janela no centro do monitor
            stage.centerOnScreen();

            // exibe as alteracoes na tela
            stage.show();
        } catch (IOException e) {
            // exibe o rastreio do erro caso o carregamento do fxml falhe
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