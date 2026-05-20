package model;

public class Curso {
    private int codcurso;
    private String nome;
    private String descricao;
    private int coddepto;

    public Curso() {}

    public Curso(int codcurso, String nome, String descricao, int coddepto) {
        this.codcurso = codcurso;
        this.nome = nome;
        this.descricao = descricao;
        this.coddepto = coddepto;
    }

    public int getCodcurso() { return codcurso; }
    public void setCodcurso(int codcurso) { this.codcurso = codcurso; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getCoddepto() { return coddepto; }
    public void setCoddepto(int coddepto) { this.coddepto = coddepto; }
}
