// define o modulo principal do nosso sistema
module org.example.gestaodefrotas {
    // exige o carregamento dos controles visuais do javafx (botoes, tabelas, etc)
    requires javafx.controls;
    // exige o carregamento do leitor de arquivos fxml do javafx
    requires javafx.fxml;

    // exige o modulo padrao de banco de dados do java
    requires java.sql;
    // exige o driver especifico do mysql para conectar ao banco
    requires mysql.connector.j;
    // exige bibliotecas de desktop padrao do java
    requires java.desktop;
    // exige a base do javafx para lidar com propriedades e bindings
    requires javafx.base;
    // exige o modulo grafico do javafx para renderizar as telas
    requires javafx.graphics;

    // abre o pacote principal para o javafx fxml poder ler e iniciar a aplicacao
    opens org.example.gestaodefrotas to javafx.fxml;
    // abre o pacote dos controladores para o javafx fxml poder linkar os botoes aos metodos
    opens org.example.gestaodefrotas.controller to javafx.fxml;

    // abre o pacote de modelos para a base do javafx poder ler os atributos e preencher as tabelas
    opens org.example.gestaodefrotas.model to javafx.base;

    // exporta o pacote principal para ele poder ser executado
    exports org.example.gestaodefrotas;
}