package org.example.gestaodefrotas.model;

// adicionamos o 'abstract' aqui (heranca)
public abstract class Veiculo {
    private int id;
    private String modelo;
    private String placa;
    private String status;
    private int kmAtual;
    private int kmUltimaRevisao;
    private double valorDiaria;

    public Veiculo() {}

    public Veiculo(String modelo, String placa, int kmAtual, double valorDiaria) {
        this.modelo = modelo;
        this.placa = placa;
        this.kmAtual = kmAtual;
        this.valorDiaria = valorDiaria;
        this.status = "DISPONIVEL";
        this.kmUltimaRevisao = kmAtual;
    }

    // getters e setters originais mantidos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getKm() { return kmAtual; }
    public void setKm(int km) { this.kmAtual = km; }
    public int getKmUltimaRevisao() { return kmUltimaRevisao; }
    public void setKmUltimaRevisao(int kmUltimaRevisao) { this.kmUltimaRevisao = kmUltimaRevisao; }
    public double getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(double valorDiaria) { this.valorDiaria = valorDiaria; }

    // aqui entra o polimorfismo! um metodo sem corpo que obriga os filhos a implementarem
    public abstract double calcularSeguro();

    // polimorfismo: cada filho vai ter que dizer quanto cobra por 1/4 de tanque
    public abstract double getTaxaReabastecimento();

    @Override
    public String toString() {
        return modelo + " (" + placa + ")";
    }
}