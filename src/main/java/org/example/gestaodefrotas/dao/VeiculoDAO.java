// pacote onde o arquivo fica
package org.example.gestaodefrotas.dao;

// importa as 3 classes do polimorfismo
import org.example.gestaodefrotas.model.Carro;
import org.example.gestaodefrotas.model.Moto;
import org.example.gestaodefrotas.model.Veiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    // 1. metodo para cadastrar
    // 1. metodo para cadastrar
    public void salvar(Veiculo veiculo) throws SQLException {
        // o sql agora inclui tipo, portas, cilindradas e o km da revisao que estava faltando
        String sql = "INSERT INTO veiculos (modelo, placa, km_atual, valor_diaria, status, tipo, portas, cilindradas, km_ultima_revisao) VALUES (?, ?, ?, ?, 'DISPONIVEL', ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getModelo());
            stmt.setString(2, veiculo.getPlaca());
            stmt.setInt(3, veiculo.getKm());
            stmt.setDouble(4, veiculo.getValorDiaria());

            // a magica da heranca: o 'instanceof' descobre qual e a classe filha verdadeira do objeto
            if (veiculo instanceof Carro) {
                // transforma o veiculo generico num carro para acessar as portas
                Carro c = (Carro) veiculo;
                stmt.setString(5, "CARRO");
                stmt.setInt(6, c.getQuantidadePortas());
                stmt.setInt(7, 0); // carro nao usa cilindrada
            } else if (veiculo instanceof Moto) {
                // transforma o veiculo generico numa moto para acessar cilindradas
                Moto m = (Moto) veiculo;
                stmt.setString(5, "MOTO");
                stmt.setInt(6, 0); // moto nao tem porta
                stmt.setInt(7, m.getCilindradas());
            }

            // envia a quilometragem inicial como a da ultima revisao
            stmt.setInt(8, veiculo.getKmUltimaRevisao());

            stmt.executeUpdate();
        }
    }

    // 2. atualiza o carro que ja existe
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

    // metodo utilitario que le a linha do banco e decide quem nasce
    private Veiculo instanciarVeiculoDoBanco(ResultSet rs) throws SQLException {
        Veiculo v;
        String tipo = rs.getString("tipo");

        // se a coluna tipo for 'moto', ele chama o new moto()
        if ("MOTO".equals(tipo)) {
            v = new Moto(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"), rs.getInt("cilindradas"));
        } else {
            // por padrao, se nao for moto, constroi um carro
            v = new Carro(rs.getString("modelo"), rs.getString("placa"), rs.getInt("km_atual"), rs.getDouble("valor_diaria"), rs.getInt("portas"));
        }

        v.setId(rs.getInt("id"));
        v.setStatus(rs.getString("status"));
        return v;
    }

    // 3. lista os disponiveis usando o construtor inteligente acima
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

    // 4. lista os quebrados (manutencao)
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

    public void finalizarManutencao(Veiculo v) throws SQLException {
        atualizarStatus(v.getId(), "DISPONIVEL");
    }

    public void atualizarStatus(int id, String status) throws SQLException {
        String sql = "UPDATE veiculos SET status = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // metodo que faz a inativacao (soft delete) do veiculo
    public void inativar(int id) throws SQLException {
        // muda o status para inativo em vez de dar um delete real
        String sql = "UPDATE veiculos SET status = 'INATIVO' WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

}