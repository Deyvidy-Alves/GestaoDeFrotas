// pacote onde o arquivo fica
package org.example.gestaodefrotas.dao;

// importa o modelo de veiculo
import org.example.gestaodefrotas.model.Veiculo;
// ferramentas de sql e listas
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// classe responsavel por gravar e ler os veiculos do mysql
public class VeiculoDAO {

    // 1. metodo para cadastrar veiculo novo
    public void salvar(Veiculo veiculo) throws SQLException {
        // o status entra fixo como 'disponivel' para todo carro novo
        String sql = "INSERT INTO veiculos (modelo, placa, km_atual, valor_diaria, status) VALUES (?, ?, ?, ?, 'DISPONIVEL')";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // preenche as interrogacoes com os dados do objeto veiculo
            stmt.setString(1, veiculo.getModelo());
            stmt.setString(2, veiculo.getPlaca());
            stmt.setInt(3, veiculo.getKm());
            stmt.setDouble(4, veiculo.getValorDiaria());
            // executa a insercao no banco
            stmt.executeUpdate();
        }
    }

    // 2. metodo usado na edicao para atualizar um carro que ja existe
    public void atualizar(Veiculo veiculo) throws SQLException {
        // comando de update filtrando pelo id
        String sql = "UPDATE veiculos SET modelo = ?, placa = ?, valor_diaria = ?, km_atual = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // preenche as interrogacoes
            stmt.setString(1, veiculo.getModelo());
            stmt.setString(2, veiculo.getPlaca());
            stmt.setDouble(3, veiculo.getValorDiaria());
            stmt.setInt(4, veiculo.getKm());
            stmt.setInt(5, veiculo.getId()); // o id e a peca chave para saber qual carro alterar
            // manda atualizar
            stmt.executeUpdate();
        }
    }

    // 3. lista apenas carros que podem ser alugados (ignora os alugados e na oficina)
    public List<Veiculo> listarDisponiveis() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        // filtra pelo status disponivel
        String sql = "SELECT * FROM veiculos WHERE status = 'DISPONIVEL'";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // monta o veiculo e joga na lista
                Veiculo v = new Veiculo(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"));
                v.setId(rs.getInt("id"));
                v.setStatus(rs.getString("status"));
                lista.add(v);
            }
        }
        return lista;
    }

    // 4. lista carros quebrados (usado na tela de oficina)
    public List<Veiculo> listarEmManutencao() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        // filtra pelo status manutencao
        String sql = "SELECT * FROM veiculos WHERE status = 'MANUTENCAO'";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Veiculo v = new Veiculo(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"));
                v.setId(rs.getInt("id"));
                v.setStatus(rs.getString("status"));
                lista.add(v);
            }
        }
        return lista;
    }

    // 5. muda o status do carro de manutencao de volta para disponivel
    public void finalizarManutencao(Veiculo v) throws SQLException {
        String sql = "UPDATE veiculos SET status = 'DISPONIVEL' WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, v.getId());
            stmt.executeUpdate();
        }
    }

    // 6. metodo generico para trocar apenas o status de um carro sem mexer no resto
    public void atualizarStatus(int id, String status) throws SQLException {
        String sql = "UPDATE veiculos SET status = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
}