package org.example.gestaodefrotas.model;

import java.time.LocalDate;

public class Cliente {
    // encapsulamento dos atributos
    private int id;
    private String nome;
    private String cpf;
    private String cnhNumero;
    private LocalDate cnhValidade;
    private String telefone;
    private String status;

    public Cliente(String nome, String cpf, String cnhNumero, LocalDate cnhValidade, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.cnhNumero = cnhNumero;
        this.cnhValidade = cnhValidade;
        this.telefone = telefone;
        this.status = "ATIVO";
    }

    public int getId(){ return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCnhNumero() { return cnhNumero; }
    public LocalDate getCnhValidade() { return cnhValidade; }
    public String getTelefone() { return telefone; }

    // a regra visual que voce pediu: inativo e inativo
    public String getStatus() {
        // se o cara nao foi excluido, mas a data da cnh passou de hoje
        if (!"EXCLUIDO".equals(this.status) && cnhValidade != null && cnhValidade.isBefore(LocalDate.now())) {
            return "INATIVO";
        }
        return status;
    }

    public java.sql.Date getCnhValidadeSQL() { return java.sql.Date.valueOf(cnhValidade); }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setCnhNumero(String cnhNumero) { this.cnhNumero = cnhNumero; }
    public void setCnhValidade(LocalDate cnhValidade) { this.cnhValidade = cnhValidade; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return nome + " (CPF: " + cpf + ") - " + getStatus();
    }
}