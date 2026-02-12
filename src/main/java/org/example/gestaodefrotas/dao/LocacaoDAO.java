package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Veiculo;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LocacaoDAO {

    public void salvar(Locacao locacao) throws SQLException {
        // ... (Seu código de salvar antigo continua igual, não mudei nada aqui)
        String sqlLocacao = "INSERT INTO locacoes (veiculo_id, cliente_id, data_retirada, data_prevista_devolucao) VALUES (?, ?, ?, ?)";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = 'ALUGADO' WHERE id = ?";

        Connection conn = ConexaoDB.conectar();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlLocacao)) {
                stmt.setInt(1, locacao.getVeiculo().getId());
                stmt.setInt(2, locacao.getCliente().getId());
                stmt.setDate(3, locacao.getDataRetiradaSQL());
                stmt.setDate(4, locacao.getDataDevolucaoPrevistaSQL());
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                stmt.setInt(1, locacao.getVeiculo().getId());
                stmt.execute();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    // --- NOVO: Buscar locações que ainda não foram devolvidas ---
    public List<Locacao> listarEmAberto() throws SQLException {
        List<Locacao> lista = new ArrayList<>();

        // O JOIN junta as tabelas Locacao + Veiculo + Cliente para termos todos os dados
        String sql = "SELECT l.*, v.modelo, v.placa, v.valor_diaria, c.nome " +
                "FROM locacoes l " +
                "INNER JOIN veiculos v ON l.veiculo_id = v.id " +
                "INNER JOIN clientes c ON l.cliente_id = c.id " +
                "WHERE l.data_real_devolucao IS NULL"; // NULL significa que não devolveu ainda

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Reconstrói o Veículo (básico)
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("veiculo_id"));
                v.setModelo(rs.getString("modelo"));
                v.setPlaca(rs.getString("placa"));
                v.setValorDiaria(rs.getDouble("valor_diaria"));

                // Reconstrói o Cliente (básico)
                Cliente c = new Cliente(rs.getString("nome"), "", "", null, "");
                c.setId(rs.getInt("cliente_id"));

                // Reconstrói a Locação
                Locacao loc = new Locacao(
                        v, c,
                        rs.getDate("data_retirada").toLocalDate(),
                        rs.getDate("data_prevista_devolucao").toLocalDate()
                );
                loc.setId(rs.getInt("id")); // Importante para saber qual locação fechar

                lista.add(loc);
            }
        }
        return lista;
    }

    // --- NOVO: Registrar a Devolução ---
    public void registrarDevolucao(Locacao loc) throws SQLException {
        String sqlUpdateLocacao = "UPDATE locacoes SET data_real_devolucao = ?, valor_total = ? WHERE id = ?";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = 'DISPONIVEL' WHERE id = ?";

        // Calcula dias corridos. Se for 0 dias (mesmo dia), cobra 1 diária.
        long dias = ChronoUnit.DAYS.between(loc.getDataRetirada(), java.time.LocalDate.now());
        if (dias == 0) dias = 1;

        double valorTotal = dias * loc.getVeiculo().getValorDiaria();

        Connection conn = ConexaoDB.conectar();
        try {
            conn.setAutoCommit(false);

            // 1. Atualiza a Locação (Data Real e Valor)
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLocacao)) {
                stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now())); // Data de hoje
                stmt.setDouble(2, valorTotal);
                stmt.setInt(3, loc.getId());
                stmt.execute();
            }

            // 2. Libera o carro (DISPONIVEL)
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                stmt.setInt(1, loc.getVeiculo().getId());
                stmt.execute();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}