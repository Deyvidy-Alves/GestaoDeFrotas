package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Carro;
import org.example.gestaodefrotas.model.Cliente;
import org.example.gestaodefrotas.model.Locacao;
import org.example.gestaodefrotas.model.Moto;
import org.example.gestaodefrotas.model.Veiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocacaoDAO {

    public int salvarERetornarId(Locacao locacao) throws SQLException {
        String sqlLocacao = "INSERT INTO locacoes (veiculo_id, cliente_id, data_retirada, data_prevista_devolucao) VALUES (?, ?, ?, ?)";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = 'ALUGADO' WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                int idGerado = 0;
                try (PreparedStatement stmt = conn.prepareStatement(sqlLocacao, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, locacao.getVeiculo().getId());
                    stmt.setInt(2, locacao.getCliente().getId());
                    stmt.setDate(3, java.sql.Date.valueOf(locacao.getDataRetirada()));
                    stmt.setDate(4, java.sql.Date.valueOf(locacao.getDataDevolucaoPrevista()));
                    stmt.execute();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idGerado = rs.getInt(1);
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateVeiculo)) {
                    stmt.setInt(1, locacao.getVeiculo().getId());
                    stmt.execute();
                }

                conn.commit();
                return idGerado;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Locacao> listarEmAberto() throws SQLException {
        List<Locacao> lista = new ArrayList<>();

        // sql corrigido com apelidos para evitar o bug de id ambiguo
        String sql = "SELECT l.id AS locacao_id, l.data_retirada, l.data_prevista_devolucao, " +
                "v.id AS veiculo_id, v.modelo, v.placa, v.valor_diaria, v.km_atual, v.tipo, " +
                "c.id AS cliente_id, c.nome " +
                "FROM locacoes l " +
                "INNER JOIN veiculos v ON l.veiculo_id = v.id " +
                "INNER JOIN clientes c ON l.cliente_id = c.id " +
                "WHERE l.data_real_devolucao IS NULL";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                Veiculo v;

                if ("MOTO".equals(tipo)) {
                    v = new Moto();
                } else {
                    v = new Carro();
                }

                // le usando o apelido certo do veiculo
                v.setId(rs.getInt("veiculo_id"));
                v.setModelo(rs.getString("modelo"));
                v.setPlaca(rs.getString("placa"));
                v.setValorDiaria(rs.getDouble("valor_diaria"));
                v.setKm(rs.getInt("km_atual"));

                Cliente c = new Cliente(rs.getString("nome"), "", "", null, "");
                // le o apelido do cliente
                c.setId(rs.getInt("cliente_id"));

                Locacao loc = new Locacao(v, c,
                        rs.getDate("data_retirada").toLocalDate(),
                        rs.getDate("data_prevista_devolucao").toLocalDate()
                );
                // le o apelido da locacao
                loc.setId(rs.getInt("locacao_id"));
                lista.add(loc);
            }
        }
        return lista;
    }

    public void registrarDevolucao(Locacao loc) throws SQLException {
        String sqlUpdateLocacao = "UPDATE locacoes SET data_real_devolucao = ?, valor_total = ? WHERE id = ?";
        String sqlUpdateVeiculo = "UPDATE veiculos SET status = ?, km_atual = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLocacao)) {
                    stmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                    stmt.setDouble(2, loc.getValorTotal());
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

    public double calcularFaturamentoTotal() throws SQLException {
        String sql = "SELECT SUM(GREATEST(valor_total, 0)) AS total FROM locacoes WHERE data_real_devolucao IS NOT NULL";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}