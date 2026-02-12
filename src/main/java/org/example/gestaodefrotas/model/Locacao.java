package org.example.gestaodefrotas.model;

import java.time.LocalDate;

public class Locacao {
    private int id;
    private Veiculo veiculo;
    private Cliente cliente;
    private LocalDate dataRetirada;
    private LocalDate dataDevolucaoPrevista;
    private double valorTotal;

    public Locacao(Veiculo veiculo, Cliente cliente, LocalDate dataRetirada, LocalDate dataDevolucaoPrevista) {
        this.veiculo = veiculo;
        this.cliente = cliente;
        this.dataRetirada = dataRetirada;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.valorTotal = 0.0;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Veiculo getVeiculo() { return veiculo; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getDataRetirada() { return dataRetirada; }
    public LocalDate getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public java.sql.Date getDataRetiradaSQL() { return java.sql.Date.valueOf(dataRetirada); }
    public java.sql.Date getDataDevolucaoPrevistaSQL() { return java.sql.Date.valueOf(dataDevolucaoPrevista); }

    @Override
    public String toString() {
        return veiculo.getModelo() + " - " + cliente.getNome();
    }

}