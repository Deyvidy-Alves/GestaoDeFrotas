package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Veiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocacaoDAO {

    // 1. MÉTODO PARA REALIZAR NOVA LOCAÇÃO (O que estava faltando!)
    public void salvar(Locacao locacao) throws SQLException {
        String sqlLocacao = "INSERT INTO locacoes (veiculo_id, cliente_id, data_retirada, data_prevista_devolucao) VALUES (?, ?, ?, ?)";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = 'ALUGADO' WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlLocacao)) {
                    stmt.setInt(1, locacao.getVeiculo().getId());
                    stmt.setInt(2, locacao.getCliente().getId());
                    stmt.setDate(3, java.sql.Date.valueOf(locacao.getDataRetirada()));
                    stmt.setDate(4, java.sql.Date.valueOf(locacao.getDataDevolucaoPrevista()));
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
            }
        }
    }

    // 2. MÉTODO PARA LISTAR CARROS ALUGADOS
    public List<Locacao> listarEmAberto() throws SQLException {
        List<Locacao> lista = new ArrayList<>();
        String sql = "SELECT l.*, v.modelo, v.placa, v.valor_diaria, v.km_atual, c.nome " +
                "FROM locacoes l " +
                "INNER JOIN veiculos v ON l.veiculo_id = v.id " +
                "INNER JOIN clientes c ON l.cliente_id = c.id " +
                "WHERE l.data_real_devolucao IS NULL";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("veiculo_id"));
                v.setModelo(rs.getString("modelo"));
                v.setPlaca(rs.getString("placa"));
                v.setValorDiaria(rs.getDouble("valor_diaria"));
                v.setKm(rs.getInt("km_atual"));

                Cliente c = new Cliente(rs.getString("nome"), "", "", null, "");
                c.setId(rs.getInt("cliente_id"));

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

    // 3. MÉTODO PARA REGISTRAR DEVOLUÇÃO (Versão única e atualizada)
    public void registrarDevolucao(Locacao loc) throws SQLException {
        String sqlUpdateLocacao = "UPDATE locacoes SET data_real_devolucao = ?, valor_total = ? WHERE id = ?";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = ?, km_atual = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLocacao)) {
                    stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                    stmt.setDouble(2, 0.0); // O valor calculado deve ser passado aqui se desejar salvar
                    stmt.setInt(3, loc.getId());
                    stmt.execute();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                    int kmNovo = loc.getVeiculo().getKm();
                    String novoStatus = (kmNovo > 0 && (kmNovo % 10000 < 500)) ? "MANUTENCAO" : "DISPONIVEL";

                    stmt.setString(1, novoStatus);
                    stmt.setInt(2, kmNovo);
                    stmt.setInt(3, loc.getVeiculo().getId());
                    stmt.execute();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}