package org.example.gestaodefrotas.model;

// heranca
public class Moto extends Veiculo {

    // atributo especifico de moto
    private int cilindradas;

    public Moto(String modelo, String placa, int kmAtual, double valorDiaria, int cilindradas) {
        // chama o pai
        super(modelo, placa, kmAtual, valorDiaria);
        this.cilindradas = cilindradas;
    }

    public Moto() {}

    public int getCilindradas() { return cilindradas; }
    public void setCilindradas(int cilindradas) { this.cilindradas = cilindradas; }

    // polimorfismo agindo aqui
    @Override
    public double calcularSeguro() {
        // moto tem muito mais risco de queda/acidente, entao o seguro e de 12% da diaria
        return getValorDiaria() * 0.12;
    }

    @Override
    public double getTaxaReabastecimento() {
        return 25.0; // r$ 25,00 por quarto de tanque
    }

}