package modelo;

/**
 * Representa uma linha da tabela FUNCIONARIO.
 *
 * O atributo "supervisor" e a propria matricula de outro funcionario
 * (auto-relacionamento).  Pode ser null (funcionario sem chefe).
 */
public class Funcionario {
    private String matricula;
    private String cpf;
    private String nome;
    private String telefone1;
    private String telefone2;
    private String tipo;          // 'operacional' ou 'administrativo'
    private String cnpjFilial;    // FK -> filial.cnpj
    private String supervisor;    // FK -> funcionario.matricula (pode ser null)

    public Funcionario() {}

    public Funcionario(String matricula, String cpf, String nome,
                       String telefone1, String telefone2,
                       String tipo, String cnpjFilial, String supervisor) {
        this.matricula  = matricula;
        this.cpf        = cpf;
        this.nome       = nome;
        this.telefone1  = telefone1;
        this.telefone2  = telefone2;
        this.tipo       = tipo;
        this.cnpjFilial = cnpjFilial;
        this.supervisor = supervisor;
    }

    public String getMatricula()           { return matricula; }
    public void   setMatricula(String v)   { this.matricula = v; }

    public String getCpf()                 { return cpf; }
    public void   setCpf(String v)         { this.cpf = v; }

    public String getNome()                { return nome; }
    public void   setNome(String v)        { this.nome = v; }

    public String getTelefone1()           { return telefone1; }
    public void   setTelefone1(String v)   { this.telefone1 = v; }

    public String getTelefone2()           { return telefone2; }
    public void   setTelefone2(String v)   { this.telefone2 = v; }

    public String getTipo()                { return tipo; }
    public void   setTipo(String v)        { this.tipo = v; }

    public String getCnpjFilial()          { return cnpjFilial; }
    public void   setCnpjFilial(String v)  { this.cnpjFilial = v; }

    public String getSupervisor()          { return supervisor; }
    public void   setSupervisor(String v)  { this.supervisor = v; }
}
