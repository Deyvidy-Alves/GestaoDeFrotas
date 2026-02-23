// localizacao do pacote
package org.example.gestaodefrotas.model;

// importa a ferramenta de data
import java.time.LocalDate;

// classe que registra o estado do carro quando sai e quando volta
public class Vistoria {

    // atributos privados para proteger os dados (encapsulamento)
    // id da vistoria no banco
    private int id;
    // o objeto da locacao ao qual esta vistoria pertence
    private Locacao locacao;
    // informa se e vistoria de "retirada" ou "devolucao"
    private String tipo;
    // quantidade de combustivel marcada no painel
    private String nivelCombustivel;
    // texto com detalhes de avarias (arranhoes, amassados, sujeira)
    private String observacoes;
    // data exata em que o funcionario fez a vistoria
    private LocalDate dataVistoria;

    // construtor completo usado se precisarmos criar uma vistoria com todos os dados de uma vez
    public Vistoria(Locacao locacao, String tipo, String nivelCombustivel, String observacoes, LocalDate dataVistoria) {
        // associa locacao
        this.locacao = locacao;
        // associa tipo
        this.tipo = tipo;
        // associa combustivel
        this.nivelCombustivel = nivelCombustivel;
        // associa observacoes
        this.observacoes = observacoes;
        // associa data
        this.dataVistoria = dataVistoria;
    }

    // construtor vazio: o java e o banco de dados as vezes precisam nascer o objeto vazio primeiro para ir preenchendo depois
    public Vistoria() {
    }

    // devolve o id da vistoria
    public int getId() {
        return id;
    }

    // altera o id da vistoria
    public void setId(int id) {
        this.id = id;
    }

    // devolve a qual locacao essa vistoria esta ligada
    public Locacao getLocacao() {
        return locacao;
    }

    // liga esta vistoria a uma locacao especifica
    public void setLocacao(Locacao locacao) {
        this.locacao = locacao;
    }

    // devolve se e retirada ou devolucao
    public String getTipo() {
        return tipo;
    }

    // define o tipo da vistoria
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // devolve como estava o tanque
    public String getNivelCombustivel() {
        return nivelCombustivel;
    }

    // define como estava o tanque
    public void setNivelCombustivel(String nivelCombustivel) {
        this.nivelCombustivel = nivelCombustivel;
    }

    // devolve as anotacoes de danos
    public String getObservacoes() {
        return observacoes;
    }

    // define as anotacoes de danos do carro
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    // devolve o dia em que a vistoria ocorreu
    public LocalDate getDataVistoria() {
        return dataVistoria;
    }

    // define o dia em que a vistoria foi feita
    public void setDataVistoria(LocalDate dataVistoria) {
        this.dataVistoria = dataVistoria;
    }
}