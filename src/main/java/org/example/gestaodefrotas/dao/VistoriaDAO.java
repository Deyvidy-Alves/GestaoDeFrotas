// diretorio padrao
package org.example.gestaodefrotas.dao;

// importa o modelo
import org.example.gestaodefrotas.model.Vistoria;
// ferramentas de banco de dados corretas
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// classe que salva as vistorias de retirada e devolucao no banco
public class VistoriaDAO {

    // metodo que ja existia para gravar a vistoria
    public void salvar(Vistoria vistoria) throws SQLException {
        String sql = "INSERT INTO vistorias (locacao_id, tipo, nivel_combustivel, observacoes, data_vistoria) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vistoria.getLocacao().getId());
            stmt.setString(2, vistoria.getTipo());
            stmt.setString(3, vistoria.getNivelCombustivel());
            stmt.setString(4, vistoria.getObservacoes());
            stmt.setDate(5, java.sql.Date.valueOf(vistoria.getDataVistoria()));

            stmt.executeUpdate();
        }
    }

    // o metodo problematico, agora no lugar certo e com o import correto!
    public String buscarCombustivelRetirada(int locacaoId) throws SQLException {
        String sql = "SELECT nivel_combustivel FROM vistorias WHERE locacao_id = ? AND tipo = 'RETIRADA' LIMIT 1";

        // usando a conexao sql certa
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, locacaoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nivel_combustivel");
                }
            }
        }
        // devolve o padrao caso de algum problema na busca
        return "cheio";
    }
}