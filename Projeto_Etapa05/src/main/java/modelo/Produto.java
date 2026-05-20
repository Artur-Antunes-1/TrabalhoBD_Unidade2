package modelo;

import java.math.BigDecimal;

/**
 * Representa uma linha da tabela PRODUTO.
 */
public class Produto {
    private String     codigo;
    private String     nome;
    private BigDecimal precoBase;

    public Produto() {}

    public Produto(String codigo, String nome, BigDecimal precoBase) {
        this.codigo    = codigo;
        this.nome      = nome;
        this.precoBase = precoBase;
    }

    public String     getCodigo()              { return codigo; }
    public void       setCodigo(String v)      { this.codigo = v; }

    public String     getNome()                { return nome; }
    public void       setNome(String v)        { this.nome = v; }

    public BigDecimal getPrecoBase()           { return precoBase; }
    public void       setPrecoBase(BigDecimal v) { this.precoBase = v; }
}
