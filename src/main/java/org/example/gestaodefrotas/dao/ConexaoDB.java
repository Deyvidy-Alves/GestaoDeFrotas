// define o pacote onde o arquivo esta guardado
package org.example.gestaodefrotas.dao;

// importa as classes necessarias para conectar com o banco de dados
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// classe responsavel por fabricar as conexoes com o mysql
public class ConexaoDB {

    // endereco de onde o banco de dados esta rodando (localhost na porta 3306, banco frotadb)
    private static final String URL = "jdbc:mysql://localhost:3306/frotadb";
    // usuario padrao do mysql
    private static final String USER = "root";
    // senha do mysql (ajuste de acordo com o seu computador)
    private static final String PASS = "Pf4fkpi0!";

    // metodo estatico que devolve uma conexao pronta para uso
    public static Connection conectar() throws SQLException {
        try {
            // usa o drivermanager para abrir a porta de comunicacao com o mysql usando a url, usuario e senha
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            // se o mysql estiver desligado ou a senha errada, ele avisa no console
            System.out.println("erro ao conectar: " + e.getMessage());
            // repassa o erro para quem chamou o metodo
            throw e;
        }
    }
}