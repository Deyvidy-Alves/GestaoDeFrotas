// localizacao do arquivo
package org.example.gestaodefrotas.dao;

// importa o modelo do cliente que criamos
import org.example.gestaodefrotas.model.Cliente;
// importa as ferramentas de banco de dados do java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// classe que faz o meio de campo entre o java e a tabela de clientes no mysql
public class ClienteDAO {

    // metodo para inserir um novo cliente no banco
    public void salvar(Cliente c) throws SQLException {
        // comando sql com interrogacoes nos lugares onde vao os dados
        String sql = "INSERT INTO clientes (nome, cpf, cnh_numero, cnh_validade, telefone) VALUES (?, ?, ?, ?, ?)";

        // abre a conexao e prepara o comando sql. o 'try' com parenteses fecha a conexao automaticamente no final
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // troca a primeira interrogacao pelo nome do cliente
            stmt.setString(1, c.getNome());
            // troca a segunda interrogacao pelo cpf
            stmt.setString(2, c.getCpf());
            // troca a terceira pelo numero da cnh
            stmt.setString(3, c.getCnhNumero());
            // troca a quarta pela data de validade (usando aquele metodo de conversao que criamos)
            stmt.setDate(4, java.sql.Date.valueOf(c.getCnhValidade()));
            // troca a quinta pelo telefone
            stmt.setString(5, c.getTelefone());

            // aperta o "enter" e manda o comando pro mysql executar
            stmt.execute();
        }
    }

    // metodo que busca todos os clientes cadastrados e devolve uma lista
    public List<Cliente> listarTodos() throws SQLException {
        // cria uma lista vazia para ir guardando os clientes que chegarem do banco
        List<Cliente> lista = new ArrayList<>();
        // comando sql para buscar tudo da tabela clientes
        String sql = "SELECT * FROM clientes";

        // conecta, prepara o comando e ja executa a busca (executequery), guardando a resposta no 'rs' (resultset)
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // enquanto tiver uma proxima linha na tabela do banco, o laco continua rodando
            while (rs.next()) {
                // cria um objeto cliente no java usando as colunas da linha atual do banco
                Cliente c = new Cliente(
                        rs.getString("nome"),          // pega a coluna nome
                        rs.getString("cpf"),           // pega a coluna cpf
                        rs.getString("cnh_numero"),    // pega a coluna da cnh
                        rs.getDate("cnh_validade").toLocalDate(), // converte a data do sql de volta para o formato do java
                        rs.getString("telefone")       // pega a coluna telefone
                );
                // seta o id que veio do banco no objeto
                c.setId(rs.getInt("id"));
                // adiciona o cliente montado dentro da nossa lista
                lista.add(c);
            }
        }
        // no final, devolve a lista cheia para a tela mostrar
        return lista;
    }
}