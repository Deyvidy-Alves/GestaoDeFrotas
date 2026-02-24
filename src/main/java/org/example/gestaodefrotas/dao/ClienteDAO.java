package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void salvar(Cliente cliente) throws SQLException {
        // prepared statement: protege contra sql injection
        String sql = "INSERT INTO clientes (nome, cpf, cnh_numero, cnh_validade, telefone, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getCnhNumero());
            stmt.setDate(4, cliente.getCnhValidadeSQL());
            stmt.setString(5, cliente.getTelefone());
            stmt.setString(6, cliente.getStatus());
            stmt.executeUpdate();
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET nome = ?, cpf = ?, cnh_numero = ?, cnh_validade = ?, telefone = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setString(2, c.getCpf());
            stmt.setString(3, c.getCnhNumero());
            stmt.setDate(4, java.sql.Date.valueOf(c.getCnhValidade()));
            stmt.setString(5, c.getTelefone());
            stmt.setInt(6, c.getId());
            stmt.executeUpdate();
        }
    }

    // soft delete agora usa a palavra correta: excluido
    public void excluir(int id) throws SQLException {
        String sql = "UPDATE clientes SET status = 'EXCLUIDO' WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        // agora traz todo mundo da base, exceto os que foram realmente excluidos.
        // ou seja: clientes ativos e inativos (vencidos) vao aparecer na tabela!
        String sql = "SELECT * FROM clientes WHERE status != 'EXCLUIDO'";
        try (Connection conn = ConexaoDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getString("nome"), rs.getString("cpf"), rs.getString("cnh_numero"),
                        rs.getDate("cnh_validade").toLocalDate(), rs.getString("telefone")
                );
                c.setId(rs.getInt("id"));
                c.setStatus(rs.getString("status"));
                clientes.add(c);
            }
        }
        return clientes;
    }
}