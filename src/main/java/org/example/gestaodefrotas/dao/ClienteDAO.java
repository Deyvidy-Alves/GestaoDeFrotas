// localizacao do arquivo
package org.example.gestaodefrotas.dao;

// importa o modelo
import org.example.gestaodefrotas.model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // metodo para inserir um novo cliente no banco
    public void salvar(Cliente c) throws SQLException {
        // adicionamos a coluna status no sql
        String sql = "INSERT INTO clientes (nome, cpf, cnh_numero, cnh_validade, telefone, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getNome());
            stmt.setString(2, c.getCpf());
            stmt.setString(3, c.getCnhNumero());
            stmt.setDate(4, java.sql.Date.valueOf(c.getCnhValidade()));
            stmt.setString(5, c.getTelefone());
            // envia o status 'ativo' para o banco
            stmt.setString(6, c.getStatus());

            stmt.execute();
        }
    }

    // metodo que faz a magica do soft delete
    public void inativar(int id) throws SQLException {
        // ao inves de delete, fazemos um update mudando a etiqueta do cliente
        String sql = "UPDATE clientes SET status = 'INATIVO' WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // aplica a inativacao no cliente especifico
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // metodo que busca clientes, mas agora so traz quem esta ativo
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        // a trava mestre: o java vai fingir que os inativos nao existem
        String sql = "SELECT * FROM clientes WHERE status = 'ATIVO'";

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
                // puxa o status do banco para o objeto
                c.setStatus(rs.getString("status"));

                lista.add(c);
            }
        }
        return lista;
    }

    // metodo para atualizar os dados de um cliente que ja existe
    public void atualizar(Cliente c) throws SQLException {
        // o comando update muda os dados onde o id for igual ao do cliente selecionado
        String sql = "UPDATE clientes SET nome = ?, cpf = ?, cnh_numero = ?, cnh_validade = ?, telefone = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getNome());
            stmt.setString(2, c.getCpf());
            stmt.setString(3, c.getCnhNumero());
            stmt.setDate(4, java.sql.Date.valueOf(c.getCnhValidade()));
            stmt.setString(5, c.getTelefone());
            // o id vai na ultima interrogacao para o banco saber quem alterar
            stmt.setInt(6, c.getId());

            stmt.executeUpdate();
        }
    }

}