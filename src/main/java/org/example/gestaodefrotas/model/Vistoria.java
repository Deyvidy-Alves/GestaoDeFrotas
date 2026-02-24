// localizacao do pacote
package org.example.gestaodefrotas.model;

// importa a ferramenta de data (e so ela!)
import java.time.LocalDate;

// classe que registra o estado do carro quando sai e quando volta
public class Vistoria {

    // id da vistoria no banco
    private int id;
    // o objeto da locacao ao qual esta vistoria pertence
    private Locacao locacao;
    // informa se e vistoria de "retirada" ou "devolucao"
    private String tipo;
    // quantidade de combustivel marcada no painel
    private String nivelCombustivel;
    // texto com detalhes de avarias
    private String observacoes;
    // data exata em que o funcionario fez a vistoria
    private LocalDate dataVistoria;

    // construtor completo
    public Vistoria(Locacao locacao, String tipo, String nivelCombustivel, String observacoes, LocalDate dataVistoria) {
        this.locacao = locacao;
        this.tipo = tipo;
        this.nivelCombustivel = nivelCombustivel;
        this.observacoes = observacoes;
        this.dataVistoria = dataVistoria;
    }

    // construtor vazio
    public Vistoria() {
    }

    // getters e setters padroes
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Locacao getLocacao() { return locacao; }
    public void setLocacao(Locacao locacao) { this.locacao = locacao; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNivelCombustivel() { return nivelCombustivel; }
    public void setNivelCombustivel(String nivelCombustivel) { this.nivelCombustivel = nivelCombustivel; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDate getDataVistoria() { return dataVistoria; }
    public void setDataVistoria(LocalDate dataVistoria) { this.dataVistoria = dataVistoria; }
}