package org.example.gestaodefrotas.model;

// o extends indica a heranca
public class Carro extends Veiculo {

    // atributo especifico que so carro tem
    private int quantidadePortas;

    public Carro(String modelo, String placa, int kmAtual, double valorDiaria, int quantidadePortas) {
        // o 'super' chama o construtor da classe pai (veiculo) para preencher o basico
        super(modelo, placa, kmAtual, valorDiaria);
        this.quantidadePortas = quantidadePortas;
    }

    public Carro() {}

    public int getQuantidadePortas() { return quantidadePortas; }
    public void setQuantidadePortas(int quantidadePortas) { this.quantidadePortas = quantidadePortas; }

    // a aplicacao do polimorfismo obrigatorio
    @Override
    public double calcularSeguro() {
        // carro tem taxa de seguro de 5% sobre o valor da diaria
        return getValorDiaria() * 0.05;
    }

    @Override
    public double getTaxaReabastecimento() {
        return 100.0; // r$ 100,00 por quarto de tanque
    }
}