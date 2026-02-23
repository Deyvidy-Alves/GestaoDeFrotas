// define o pacote do arquivo
package org.example.gestaodefrotas.model;

// importa ferramenta de data
import java.time.LocalDate;

// declaracao da classe locacao (o contrato de aluguel)
public class Locacao {
    // id unico do contrato no banco
    private int id;
    // o objeto veiculo inteiro que esta sendo alugado
    private Veiculo veiculo;
    // o objeto cliente inteiro que esta alugando
    private Cliente cliente;
    // a data em que o carro foi retirado da agencia
    private LocalDate dataRetirada;
    // a data combinada para o cliente devolver o carro
    private LocalDate dataDevolucaoPrevista;
    // o valor final cobrado (so e preenchido na devolucao)
    private double valorTotal;

    // construtor chamado na hora de fazer uma nova locacao na tela
    public Locacao(Veiculo veiculo, Cliente cliente, LocalDate dataRetirada, LocalDate dataDevolucaoPrevista) {
        // guarda o veiculo escolhido
        this.veiculo = veiculo;
        // guarda o cliente escolhido
        this.cliente = cliente;
        // guarda a data de retirada
        this.dataRetirada = dataRetirada;
        // guarda a data de devolucao combinada
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        // como o contrato acabou de ser aberto, o valor comeca zerado
        this.valorTotal = 0.0;
    }

    // devolve o id da locacao
    public int getId() { return id; }
    // define o id da locacao
    public void setId(int id) { this.id = id; }
    // devolve o objeto veiculo amarrado a esta locacao
    public Veiculo getVeiculo() { return veiculo; }
    // devolve o objeto cliente amarrado a esta locacao
    public Cliente getCliente() { return cliente; }
    // devolve a data de retirada
    public LocalDate getDataRetirada() { return dataRetirada; }
    // devolve a data de previsao de devolucao
    public LocalDate getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    // devolve o valor total cobrado
    public double getValorTotal() { return valorTotal; }
    // define o valor total (usado na tela de devolucao apos os calculos)
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    // converte a data de retirada para o padrao sql para salvar no banco
    public java.sql.Date getDataRetiradaSQL() { return java.sql.Date.valueOf(dataRetirada); }
    // converte a data de previsao para o padrao sql para salvar no banco
    public java.sql.Date getDataDevolucaoPrevistaSQL() { return java.sql.Date.valueOf(dataDevolucaoPrevista); }

    // ensina o java a mostrar essa locacao como texto na tela
    @Override
    public String toString() {
        // aparece na caixa de selecao da devolucao como: "modelo do carro - nome do cliente"
        return veiculo.getModelo() + " - " + cliente.getNome();
    }
}