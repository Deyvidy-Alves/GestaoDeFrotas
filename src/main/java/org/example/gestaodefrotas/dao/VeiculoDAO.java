package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Veiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    public void salvar(Veiculo v) throws SQLException {
        String sql = "INSERT INTO veiculos (modelo, placa, status, km_atual, km_ultima_revisao, valor_diaria) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getModelo());
            stmt.setString(2, v.getPlaca());
            stmt.setString(3, v.getStatus());
            stmt.setInt(4, v.getKm());
            stmt.setInt(5, v.getKmUltimaRevisao());
            stmt.setDouble(6, v.getValorDiaria());
            stmt.execute();
        }
    }

    public List<Veiculo> listarDisponiveis() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM veiculos WHERE status = 'DISPONIVEL'";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("id"));
                v.setModelo(rs.getString("modelo"));
                v.setPlaca(rs.getString("placa"));
                v.setStatus(rs.getString("status"));
                v.setKm(rs.getInt("km_atual"));
                v.setKmUltimaRevisao(rs.getInt("km_ultima_revisao"));
                v.setValorDiaria(rs.getDouble("valor_diaria"));
                lista.add(v);
            }
        }
        return lista;
    }
}