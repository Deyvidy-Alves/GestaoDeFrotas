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
    // atributo privado para o status de atividade (soft delete)
    private String status;

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
        // seta o status inicial ativo
        this.status = "ATIVO";
    }

    // getters (para ler os dados)
    public int getId(){ return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCnhNumero() { return cnhNumero; }
    public LocalDate getCnhValidade() { return cnhValidade; }
    public String getTelefone() { return telefone; }
    public String getStatus() { return status; }

    // metodo utilitario que converte o localdate para o formato de data que o banco sql entende
    public java.sql.Date getCnhValidadeSQL() { return java.sql.Date.valueOf(cnhValidade); }

    // setters (para alterar os dados na tela de edicao e banco)
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setCnhNumero(String cnhNumero) { this.cnhNumero = cnhNumero; }
    public void setCnhValidade(LocalDate cnhValidade) { this.cnhValidade = cnhValidade; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setStatus(String status) { this.status = status; }

    // sobrescreve o metodo padrao que transforma o objeto em texto
    @Override
    public String toString() {
        // define como o cliente vai aparecer nas caixas de selecao (combobox): "nome (cpf: 000...)"
        return nome + " (CPF: " + cpf + ")";
    }
}