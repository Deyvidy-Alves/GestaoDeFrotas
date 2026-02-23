// diretorio padrao
package org.example.gestaodefrotas.dao;

// importa o modelo
import org.example.gestaodefrotas.model.Vistoria;
// ferramentas de banco
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// classe que salva as vistorias de retirada e devolucao
public class VistoriaDAO {

    public void salvar(Vistoria vistoria) throws SQLException {
        // instrucao sql para inserir os dados da vistoria amarrados a uma locacao especifica (locacao_id)
        String sql = "INSERT INTO vistorias (locacao_id, tipo, nivel_combustivel, observacoes, data_vistoria) VALUES (?, ?, ?, ?, ?)";

        // abre a conexao com o banco
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // pega o id da locacao que esta dentro do objeto locacao dentro da vistoria
            stmt.setInt(1, vistoria.getLocacao().getId());
            // preenche se e retirada ou devolucao
            stmt.setString(2, vistoria.getTipo());
            // preenche se o tanque ta cheio, vazio, etc
            stmt.setString(3, vistoria.getNivelCombustivel());
            // preenche os amassados e detalhes
            stmt.setString(4, vistoria.getObservacoes());
            // converte a data e envia
            stmt.setDate(5, java.sql.Date.valueOf(vistoria.getDataVistoria()));

            // executa a gravacao permanente no mysql
            stmt.executeUpdate();
        }
    }
}