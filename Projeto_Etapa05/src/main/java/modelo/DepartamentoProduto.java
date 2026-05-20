package modelo;

/**
 * Representa uma linha da tabela DEPARTAMENTO_PRODUTO (associacao N:N
 * entre departamento e produto).  PK composta: (cod_departamento, cod_produto).
 */
public class DepartamentoProduto {
    private String codDepartamento;
    private String codProduto;

    public DepartamentoProduto() {}

    public DepartamentoProduto(String codDepartamento, String codProduto) {
        this.codDepartamento = codDepartamento;
        this.codProduto      = codProduto;
    }

    public String getCodDepartamento()         { return codDepartamento; }
    public void   setCodDepartamento(String v) { this.codDepartamento = v; }

    public String getCodProduto()              { return codProduto; }
    public void   setCodProduto(String v)      { this.codProduto = v; }
}
