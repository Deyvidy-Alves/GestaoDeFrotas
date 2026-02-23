// define a localizacao do arquivo
package org.example.gestaodefrotas.model;

// classe que representa os carros da frota
public class Veiculo {
    // id do carro no banco
    private int id;
    // modelo do carro (ex: fiat uno)
    private String modelo;
    // placa do carro
    private String placa;
    // status atual (disponivel, alugado, manutencao)
    private String status;
    // quilometragem atual marcada no painel
    private int kmAtual;
    // quilometragem de quando o carro foi pra oficina da ultima vez
    private int kmUltimaRevisao;
    // preco cobrado por um dia de aluguel
    private double valorDiaria;

    // construtor vazio (usado pelo dao quando busca os dados aos pedacos no banco)
    public Veiculo() {}

    // construtor cheio, usado quando o usuario cadastra um carro novo na tela
    public Veiculo(String modelo, String placa, int kmAtual, double valorDiaria) {
        // guarda o modelo
        this.modelo = modelo;
        // guarda a placa
        this.placa = placa;
        // guarda o km
        this.kmAtual = kmAtual;
        // guarda o valor
        this.valorDiaria = valorDiaria;
        // todo carro novo cadastrado nasce com status disponivel
        this.status = "DISPONIVEL";
        // como e carro recem cadastrado, assumimos que o km atual e o da ultima revisao
        this.kmUltimaRevisao = kmAtual;
    }

    // pega o id
    public int getId() { return id; }
    // muda o id
    public void setId(int id) { this.id = id; }
    // pega o modelo
    public String getModelo() { return modelo; }
    // muda o modelo
    public void setModelo(String modelo) { this.modelo = modelo; }
    // pega a placa
    public String getPlaca() { return placa; }
    // muda a placa
    public void setPlaca(String placa) { this.placa = placa; }
    // pega o status
    public String getStatus() { return status; }
    // muda o status
    public void setStatus(String status) { this.status = status; }

    // metodos chamados de getkm e setkm para facilitar e bater com o propertyvaluefactory do javafx
    public int getKm() { return kmAtual; }
    // atualiza o km do carro
    public void setKm(int km) { this.kmAtual = km; }

    // pega o km da ultima revisao
    public int getKmUltimaRevisao() { return kmUltimaRevisao; }
    // atualiza o km da revisao (usado pelo manutencaocontroller)
    public void setKmUltimaRevisao(int kmUltimaRevisao) { this.kmUltimaRevisao = kmUltimaRevisao; }
    // pega o valor da diaria
    public double getValorDiaria() { return valorDiaria; }
    // atualiza o valor da diaria
    public void setValorDiaria(double valorDiaria) { this.valorDiaria = valorDiaria; }

    // converte objeto em texto para visualizacao
    @Override
    public String toString() {
        // aparece na combobox como "modelo (placa)"
        return modelo + " (" + placa + ")";
    }
}