package modelo;

import java.time.LocalDateTime;

/**
 * Representa uma linha da tabela LOG_ALTERACOES, alimentada por triggers.
 */
public class LogAlteracao {
    private int           id;
    private String        tabela;
    private String        tipoEvento;
    private String        descricao;
    private LocalDateTime dataHora;

    public LogAlteracao() {}

    public LogAlteracao(int id, String tabela, String tipoEvento,
                        String descricao, LocalDateTime dataHora) {
        this.id         = id;
        this.tabela     = tabela;
        this.tipoEvento = tipoEvento;
        this.descricao  = descricao;
        this.dataHora   = dataHora;
    }

    public int            getId()                    { return id; }
    public String         getTabela()                { return tabela; }
    public String         getTipoEvento()            { return tipoEvento; }
    public String         getDescricao()             { return descricao; }
    public LocalDateTime  getDataHora()              { return dataHora; }
}
