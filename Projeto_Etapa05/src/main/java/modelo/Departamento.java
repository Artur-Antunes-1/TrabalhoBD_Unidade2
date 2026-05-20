package modelo;

/**
 * Representa uma linha da tabela DEPARTAMENTO.
 * Apenas atributos + getters/setters (POJO).
 */
public class Departamento {
    private String codigo;
    private String categoria;

    public Departamento() {}

    public Departamento(String codigo, String categoria) {
        this.codigo    = codigo;
        this.categoria = categoria;
    }

    public String getCodigo()            { return codigo; }
    public void   setCodigo(String v)    { this.codigo = v; }

    public String getCategoria()         { return categoria; }
    public void   setCategoria(String v) { this.categoria = v; }
}
