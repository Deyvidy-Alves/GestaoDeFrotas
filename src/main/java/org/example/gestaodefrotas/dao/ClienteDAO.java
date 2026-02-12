package org.example.gestaodefrotas.dao;

import org.example.gestaodefrotas.model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void salvar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (nome, cpf, cnh_numero, cnh_validade, telefone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setString(2, c.getCpf());
            stmt.setString(3, c.getCnhNumero());
            stmt.setDate(4, java.sql.Date.valueOf(c.getCnhValidade()));
            stmt.setString(5, c.getTelefone());
            stmt.execute();
        }
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("cnh_numero"),
                        rs.getDate("cnh_validade").toLocalDate(),
                        rs.getString("telefone")
                );
                c.setId(rs.getInt("id"));
                lista.add(c);
            }
        }
        return lista;
    }
}