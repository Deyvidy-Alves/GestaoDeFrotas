package org.example.gestaodefrotas.model;

import java.time.LocalDate;

public class Cliente {
    private int id;
    private String nome;
    private String cpf;
    private String cnhNumero;
    private LocalDate cnhValidade;
    private String telefone;

    public Cliente(String nome, String cpf, String cnhNumero, LocalDate cnhValidade, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.cnhNumero = cnhNumero;
        this.cnhValidade = cnhValidade;
        this.telefone = telefone;
    }

    public int getId(){ return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCnhNumero() { return cnhNumero; }
    public LocalDate getCnhValidade() { return cnhValidade; }
    public String getTelefone() { return telefone; }
    public java.sql.Date getCnhValidadeSQL() { return java.sql.Date.valueOf(cnhValidade); }

    @Override
    public String toString() {
        return nome + " (CPF: " + cpf + ")";
    }

}