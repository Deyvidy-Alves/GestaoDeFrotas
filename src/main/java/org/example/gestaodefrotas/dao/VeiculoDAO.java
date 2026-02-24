package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Carro;
import org.example.gestaodefrotas.model.Moto;
import org.example.gestaodefrotas.model.Veiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    // metodo para cadastrar veiculo novo (carro ou moto)
    public void salvar(Veiculo veiculo) throws SQLException {
        // sql com a coluna de controle de revisao
        String sql = "INSERT INTO veiculos (modelo, placa, km_atual, valor_diaria, status, tipo, portas, cilindradas, km_ultima_revisao) VALUES (?, ?, ?, ?, 'DISPONIVEL', ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getModelo());
            stmt.setString(2, veiculo.getPlaca());
            stmt.setInt(3, veiculo.getKm());
            stmt.setDouble(4, veiculo.getValorDiaria());

            // polimorfismo: descobre se o objeto e carro ou moto para salvar os detalhes certos
            if (veiculo instanceof Carro) {
                stmt.setString(5, "CARRO");
                stmt.setInt(6, ((Carro) veiculo).getQuantidadePortas());
                stmt.setInt(7, 0);
            } else if (veiculo instanceof Moto) {
                stmt.setString(5, "MOTO");
                stmt.setInt(6, 0);
                stmt.setInt(7, ((Moto) veiculo).getCilindradas());
            }

            // no cadastro inicial, o km da ultima revisao e o km de fabrica dele
            stmt.setInt(8, veiculo.getKm());
            stmt.executeUpdate();
        }
    }

    public void atualizar(Veiculo veiculo) throws SQLException {
        String sql = "UPDATE veiculos SET modelo = ?, placa = ?, valor_diaria = ?, km_atual = ?, portas = ?, cilindradas = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, veiculo.getModelo());
            stmt.setString(2, veiculo.getPlaca());
            stmt.setDouble(3, veiculo.getValorDiaria());
            stmt.setInt(4, veiculo.getKm());

            if (veiculo instanceof Carro) {
                stmt.setInt(5, ((Carro) veiculo).getQuantidadePortas());
                stmt.setInt(6, 0);
            } else if (veiculo instanceof Moto) {
                stmt.setInt(5, 0);
                stmt.setInt(6, ((Moto) veiculo).getCilindradas());
            }

            stmt.setInt(7, veiculo.getId());
            stmt.executeUpdate();
        }
    }

    // metodo que transforma a linha do mysql em objeto java
    private Veiculo instanciarVeiculoDoBanco(ResultSet rs) throws SQLException {
        Veiculo v;
        String tipo = rs.getString("tipo");

        if ("MOTO".equals(tipo)) {
            v = new Moto(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"), rs.getInt("cilindradas"));
        } else {
            v = new Carro(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"), rs.getInt("portas"));
        }

        v.setId(rs.getInt("id"));
        v.setStatus(rs.getString("status"));

        v.setKmUltimaRevisao(rs.getInt("km_ultima_revisao"));

        return v;
    }

    public List<Veiculo> listarDisponiveis() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM veiculos WHERE status = 'DISPONIVEL'";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(instanciarVeiculoDoBanco(rs));
            }
        }
        return lista;
    }

    public List<Veiculo> listarEmManutencao() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM veiculos WHERE status = 'MANUTENCAO'";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(instanciarVeiculoDoBanco(rs));
            }
        }
        return lista;
    }

    // metodo que tira o veiculo da oficina e renova o ciclo de 1.000 ou 10.000 km
    public void finalizarManutencao(int veiculoId) throws SQLException {
        // o segredo: status vira disponivel e km_ultima_revisao vira o km_atual
        String sql = "UPDATE veiculos SET status = 'DISPONIVEL', km_ultima_revisao = km_atual WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, veiculoId);
            stmt.executeUpdate();
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE veiculos SET status = 'INATIVO' WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean placaExiste(String placa, int idAtual) throws SQLException {
        String sql = "SELECT id FROM veiculos WHERE placa = ? AND id != ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, placa);
            stmt.setInt(2, idAtual);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}