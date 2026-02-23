// pacote onde o arquivo esta localizado
package org.example.gestaodefrotas.controller;

// importacoes graficas e funcionais do javafx
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
// importacoes dos daos e modelos que o aluguel vai usar
import org.example.gestaodefrotas.HelloApplication;
import org.example.gestaodefrotas.dao.ClienteDAO;
import org.example.gestaodefrotas.dao.LocacaoDAO;
import org.example.gestaodefrotas.dao.VeiculoDAO;
import org.example.gestaodefrotas.dao.VistoriaDAO;
import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Veiculo;
import org.example.gestaodefrotas.model.Vistoria;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

// classe que cuida de abrir um novo contrato de aluguel
public class LocacaoController {

    // caixa de selecao (dropdown) para mostrar os veiculos disponiveis
    @FXML private ComboBox<Veiculo> cbVeiculo;
    // caixa de selecao para mostrar quem vai alugar
    @FXML private ComboBox<Cliente> cbCliente;
    // calendario para escolher o dia que o cara ta levando o carro
    @FXML private DatePicker dtRetirada;
    // calendario para escolher o dia que ele promete devolver
    @FXML private DatePicker dtDevolucao;
    // caixa de selecao pra dizer como ta o tanque do carro na saida
    @FXML private ComboBox<String> cbCombustivel;
    // campo de texto pra anotar se ja tem algum arranhao no carro
    @FXML private TextField txtObservacoes;

    // acionado pelo java assim que a tela abre
    @FXML
    public void initialize() {
        try {
            // puxa as listas do banco de dados pra preencher as caixinhas na tela
            carregarListas();
        } catch (SQLException e) {
            // se o mysql der pau, avisa na tela
            mostrarAlerta("erro", "falha ao carregar listas: " + e.getMessage());
        }
    }

    // vai no banco e preenche tudo
    private void carregarListas() throws SQLException {
        // busca no veiculodao so os carros que estao parados no patio (disponivel)
        cbVeiculo.getItems().addAll(new VeiculoDAO().listarDisponiveis());

        // busca todos os clientes e joga na lista
        cbCliente.getItems().addAll(new ClienteDAO().listarTodos());

        // enche a caixa de combustivel com as opcoes padrao que o funcionario pode escolher
        cbCombustivel.getItems().addAll("cheio", "3/4", "meio tanque (1/2)", "1/4", "reserva");
    }

    // o que acontece quando o cara clica em salvar
    @FXML
    protected void onSalvar() {
        try {
            // etapa 1: checa se tem campo em branco. se tiver, joga um erro e para tudo
            if (cbVeiculo.getValue() == null) throw new Exception("selecione um veículo");
            if (cbCliente.getValue() == null) throw new Exception("selecione um cliente");
            if (dtRetirada.getValue() == null || dtDevolucao.getValue() == null) throw new Exception("selecione as datas");
            if (cbCombustivel.getValue() == null) throw new Exception("informe o nível de combustível");

            // pega quem o funcionario escolheu na lista
            Cliente clienteSelecionado = cbCliente.getValue();

            // etapa 2: verifica se o cara ta com a cnh vencida
            java.time.LocalDate hoje = java.time.LocalDate.now();
            java.time.LocalDate vencimentoCnh = clienteSelecionado.getCnhValidadeSQL().toLocalDate();

            // isbefore checa se a validade passou da data de hoje
            if (vencimentoCnh != null && vencimentoCnh.isBefore(hoje)) {
                // barra o aluguel se a cnh estiver vencida
                mostrarAlerta("operação bloqueada 🚫", "cliente com cnh vencida!");
                return;
            }

            // etapa 3: monta o contrato na memoria com as informacoes da tela
            Locacao loc = new Locacao(
                    cbVeiculo.getValue(),
                    clienteSelecionado,
                    dtRetirada.getValue(),
                    dtDevolucao.getValue()
            );

            // o dao vai la no mysql, salva o contrato e traz de volta qual protocolo (id) ele ganhou
            int idGerado = new LocacaoDAO().salvarERetornarId(loc);
            // amarra o id no contrato da memoria para usarmos na vistoria
            loc.setId(idGerado);

            // etapa 4: cria a folha de vistoria de retirada
            Vistoria vis = new Vistoria();
            // amarra a vistoria ao contrato que acabamos de gerar
            vis.setLocacao(loc);
            vis.setTipo("RETIRADA");
            vis.setNivelCombustivel(cbCombustivel.getValue());
            vis.setObservacoes(txtObservacoes.getText());
            vis.setDataVistoria(hoje);

            // grava a vistoria la na tabela vistorias do banco
            new VistoriaDAO().salvar(vis);

            // etapa 5: deu tudo certo, avisa o usuario
            mostrarAlerta("sucesso", "locação e vistoria de retirada registradas com sucesso!");
            // zera os campos pra proxima
            limpar();

            // limpa a combobox de veiculos e recarrega. isso faz o carro que vc acabou de alugar sumir das opcoes!
            cbVeiculo.getItems().clear();
            cbVeiculo.getItems().addAll(new VeiculoDAO().listarDisponiveis());

        } catch (Exception e) {
            // captura os erros forcados ali em cima ou erros de banco
            mostrarAlerta("erro", e.getMessage());
        }
    }

    // o clico botao voltar ao menu
    @FXML
    protected void onVoltar(ActionEvent event) {
        try {
            // carrega as pecas visuais do menu
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // pega a janela de vidro atual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // troca o texto de cima da janela
            stage.setTitle("gestão de frota");
            // projeta a tela do menu
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // deixa a tela limpinha de novo
    private void limpar() {
        cbVeiculo.setValue(null);
        cbCliente.setValue(null);
        // data de retirada volta a ser hoje automaticamente
        dtRetirada.setValue(LocalDate.now());
        dtDevolucao.setValue(null);
        cbCombustivel.setValue(null);
        txtObservacoes.clear();
    }

    // o nosso gerador de popups padrao
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}