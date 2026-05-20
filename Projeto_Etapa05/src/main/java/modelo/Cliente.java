package modelo;

/**
 * Representa uma linha da tabela CLIENTE.
 * Apenas atributos + getters/setters (POJO).
 */
public class Cliente {
    private String cpf;
    private String nome;
    private String telefone;

    public Cliente() {}

    public Cliente(String cpf, String nome, String telefone) {
        this.cpf      = cpf;
        this.nome     = nome;
        this.telefone = telefone;
    }

    public String getCpf()              { return cpf; }
    public void   setCpf(String v)      { this.cpf = v; }

    public String getNome()             { return nome; }
    public void   setNome(String v)     { this.nome = v; }

    public String getTelefone()         { return telefone; }
    public void   setTelefone(String v) { this.telefone = v; }
}
