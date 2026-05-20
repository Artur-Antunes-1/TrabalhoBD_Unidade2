package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma linha da tabela RESUMO_VENDAS_FUNCIONARIO,
 * alimentada pelo procedimento pr_gerar_resumo_funcionarios (CURSOR).
 */
public class ResumoFuncionario {
    private String        matricula;
    private String        nome;
    private int           totalVendas;
    private BigDecimal    valorTotal;
    private String        classificacao;
    private LocalDateTime dataGeracao;

    public ResumoFuncionario() {}

    public ResumoFuncionario(String matricula, String nome, int totalVendas,
                             BigDecimal valorTotal, String classificacao,
                             LocalDateTime dataGeracao) {
        this.matricula     = matricula;
        this.nome          = nome;
        this.totalVendas   = totalVendas;
        this.valorTotal    = valorTotal;
        this.classificacao = classificacao;
        this.dataGeracao   = dataGeracao;
    }

    public String        getMatricula()     { return matricula; }
    public String        getNome()          { return nome; }
    public int           getTotalVendas()   { return totalVendas; }
    public BigDecimal    getValorTotal()    { return valorTotal; }
    public String        getClassificacao() { return classificacao; }
    public LocalDateTime getDataGeracao()   { return dataGeracao; }
}
