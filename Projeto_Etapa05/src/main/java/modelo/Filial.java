package modelo;

/**
 * Representa uma linha da tabela FILIAL.
 */
public class Filial {
    private String cnpj;
    private String rua;
    private String numero;
    private String cep;

    public Filial() {}

    public Filial(String cnpj, String rua, String numero, String cep) {
        this.cnpj   = cnpj;
        this.rua    = rua;
        this.numero = numero;
        this.cep    = cep;
    }

    public String getCnpj()           { return cnpj; }
    public void   setCnpj(String v)   { this.cnpj = v; }

    public String getRua()            { return rua; }
    public void   setRua(String v)    { this.rua = v; }

    public String getNumero()         { return numero; }
    public void   setNumero(String v) { this.numero = v; }

    public String getCep()            { return cep; }
    public void   setCep(String v)    { this.cep = v; }
}
