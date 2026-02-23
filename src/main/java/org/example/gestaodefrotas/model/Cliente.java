// indica em qual pasta este arquivo esta localizado
package org.example.gestaodefrotas.model;

// importa a ferramenta do java para trabalhar com datas (sem fuso horario)
import java.time.LocalDate;

// declaracao da classe cliente, que representa um cliente na vida real
public class Cliente {
    // atributo privado que guarda o numero unico de identificacao no banco de dados
    private int id;
    // atributo privado para o nome completo do cliente
    private String nome;
    // atributo privado para o cpf
    private String cpf;
    // atributo privado para o numero da cnh do cliente
    private String cnhNumero;
    // atributo privado que guarda a data em que a cnh vence
    private LocalDate cnhValidade;
    // atributo privado para o numero de telefone
    private String telefone;

    // construtor da classe: metodo especial chamado quando criamos um cliente novo (usando 'new cliente')
    public Cliente(String nome, String cpf, String cnhNumero, LocalDate cnhValidade, String telefone) {
        // pega o nome passado no construtor e salva no atributo da classe
        this.nome = nome;
        // salva o cpf
        this.cpf = cpf;
        // salva o numero da cnh
        this.cnhNumero = cnhNumero;
        // salva a data de validade da cnh
        this.cnhValidade = cnhValidade;
        // salva o telefone
        this.telefone = telefone;
    }

    // metodo que devolve o id do cliente
    public int getId(){ return id; }
    // metodo que permite alterar o id do cliente (geralmente usado apos salvar no banco)
    public void setId(int id) { this.id = id; }

    // metodo que devolve o nome do cliente
    public String getNome() { return nome; }
    // metodo que devolve o cpf do cliente
    public String getCpf() { return cpf; }
    // metodo que devolve o numero da cnh
    public String getCnhNumero() { return cnhNumero; }
    // metodo que devolve a validade da cnh no formato original localdate
    public LocalDate getCnhValidade() { return cnhValidade; }
    // metodo que devolve o telefone do cliente
    public String getTelefone() { return telefone; }
    // metodo utilitario que converte o localdate para o formato de data que o banco sql entende
    public java.sql.Date getCnhValidadeSQL() { return java.sql.Date.valueOf(cnhValidade); }

    // sobrescreve o metodo padrao que transforma o objeto em texto
    @Override
    public String toString() {
        // define como o cliente vai aparecer nas caixas de selecao (combobox): "nome (cpf: 000...)"
        return nome + " (CPF: " + cpf + ")";
    }
}