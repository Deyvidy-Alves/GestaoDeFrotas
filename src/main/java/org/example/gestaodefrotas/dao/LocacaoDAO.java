package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Veiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocacaoDAO {

    // 1. salva o contrato de aluguel e devolve o numero do protocolo (id) para a vistoria usar depois
    public int salvarERetornarId(Locacao locacao) throws SQLException {
        String sqlLocacao = "INSERT INTO locacoes (veiculo_id, cliente_id, data_retirada, data_prevista_devolucao) VALUES (?, ?, ?, ?)";
        // quando aluga, tira o carro da vitrine
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = 'ALUGADO' WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            // desativa o salvamento automatico. isso cria uma transacao: ou tudo da certo, ou tudo e cancelado
            conn.setAutoCommit(false);

            try {
                int idGerado = 0;

                // prepara o insert pedindo para o banco devolver qual id ele gerou automaticamente
                try (PreparedStatement stmt = conn.prepareStatement(sqlLocacao, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, locacao.getVeiculo().getId());
                    stmt.setInt(2, locacao.getCliente().getId());
                    stmt.setDate(3, java.sql.Date.valueOf(locacao.getDataRetirada()));
                    stmt.setDate(4, java.sql.Date.valueOf(locacao.getDataDevolucaoPrevista()));
                    stmt.execute();

                    // recupera o numero do id criado pelo mysql
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idGerado = rs.getInt(1); // guarda o numero
                        }
                    }
                }

                // atualiza o status do veiculo na tabela de veiculos
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                    stmt.setInt(1, locacao.getVeiculo().getId());
                    stmt.execute();
                }

                // se chegou ate aqui e nao deu erro em nada, ele salva tudo definitivamente no banco
                conn.commit();
                return idGerado;

            } catch (SQLException e) {
                // se deu erro em qualquer etapa (ex: banco caiu na metade), ele desfaz as alteracoes feitas
                conn.rollback();
                throw e; // cospe o erro pra fora
            }
        }
    }

    // 2. lista todos os contratos que o cliente ainda esta com o carro (data de devolucao nula)
    public List<Locacao> listarEmAberto() throws SQLException {
        List<Locacao> lista = new ArrayList<>();
        // uma consulta gigante (join) que costura as tabelas de locacao, veiculo e cliente tudo de uma vez
        String sql = "SELECT l.*, v.modelo, v.placa, v.valor_diaria, v.km_atual, c.nome " +
                "FROM locacoes l " +
                "INNER JOIN veiculos v ON l.veiculo_id = v.id " +
                "INNER JOIN clientes c ON l.cliente_id = c.id " +
                "WHERE l.data_real_devolucao IS NULL";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // controi um objeto veiculo temporario so com os dados que precisamos pra mostrar na tela
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("veiculo_id"));
                v.setModelo(rs.getString("modelo"));
                v.setPlaca(rs.getString("placa"));
                v.setValorDiaria(rs.getDouble("valor_diaria"));
                v.setKm(rs.getInt("km_atual"));

                // constroi o cliente com o nome que veio na consulta
                Cliente c = new Cliente(rs.getString("nome"), "", "", null, "");
                c.setId(rs.getInt("cliente_id"));

                // junta tudo no objeto locacao final e joga na lista
                Locacao loc = new Locacao(v, c,
                        rs.getDate("data_retirada").toLocalDate(),
                        rs.getDate("data_prevista_devolucao").toLocalDate()
                );
                loc.setId(rs.getInt("id"));
                lista.add(loc);
            }
        }
        return lista;
    }

    // 3. encerra a locacao, cobra o dinheiro e atualiza o km do carro
    public void registrarDevolucao(Locacao loc) throws SQLException {
        // preenche a data atual na coluna de devolucao e o dinheiro ganho
        String sqlUpdateLocacao = "UPDATE locacoes SET data_real_devolucao = ?, valor_total = ? WHERE id = ?";
        // devolve o carro pra vitrine (ou manda pra oficina) e atualiza o painel
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = ?, km_atual = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            // abre a transacao de seguranca
            conn.setAutoCommit(false);
            try {
                // salva o encerramento do contrato
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLocacao)) {
                    stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                    stmt.setDouble(2, loc.getValorTotal()); // o valor calculado la na tela vem pra ca
                    stmt.setInt(3, loc.getId());
                    stmt.execute();
                }

                // atualiza as informacoes do carro devolvido
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                    int kmNovo = loc.getVeiculo().getKm();
                    // inteligenia de frota: se o km rodado bateu um multiplo de 10.000, marca para manutencao, senao deixa disponivel
                    String novoStatus = (kmNovo > 0 && (kmNovo % 10000 < 500)) ? "MANUTENCAO" : "DISPONIVEL";

                    stmt.setString(1, novoStatus);
                    stmt.setInt(2, kmNovo);
                    stmt.setInt(3, loc.getVeiculo().getId());
                    stmt.execute();
                }
                // conclui as gravacoes
                conn.commit();
            } catch (SQLException e) {
                // cancela tudo em caso de erro
                conn.rollback();
                throw e;
            }
        }
    }

    // pegatodo o dinheiro ja ganho pelas locacoes finalizadas
    public double calcularFaturamentoTotal() throws SQLException {
        // funcao sum soma os valores. o greatest evita que numeros negativos bugados estraguem a conta
        String sql = "SELECT SUM(GREATEST(valor_total, 0)) AS total FROM locacoes WHERE data_real_devolucao IS NOT NULL";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // devolve o valor de R$ total somado
                return rs.getDouble("total");
            }
        }
        // se nao tiver nada, devolve zero
        return 0.0;
    }
}