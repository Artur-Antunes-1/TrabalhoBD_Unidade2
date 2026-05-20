package model;

public class Departamento {
    private int coddepto;
    private String nome;

    public Departamento() {}

    public Departamento(int coddepto, String nome) {
        this.coddepto = coddepto;
        this.nome = nome;
    }

    public int getCoddepto() { return coddepto; }
    public void setCoddepto(int coddepto) { this.coddepto = coddepto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
