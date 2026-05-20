package modelo;

/**
 * Representa uma linha da tabela VENDE_PRODUTO (item de uma venda).
 */
public class ItemVenda {
    private String nfe;
    private String codProduto;
    private int    quantidade;

    public ItemVenda() {}

    public ItemVenda(String nfe, String codProduto, int quantidade) {
        this.nfe        = nfe;
        this.codProduto = codProduto;
        this.quantidade = quantidade;
    }

    public String getNfe()                 { return nfe; }
    public void   setNfe(String v)         { this.nfe = v; }

    public String getCodProduto()          { return codProduto; }
    public void   setCodProduto(String v)  { this.codProduto = v; }

    public int    getQuantidade()          { return quantidade; }
    public void   setQuantidade(int v)     { this.quantidade = v; }
}
