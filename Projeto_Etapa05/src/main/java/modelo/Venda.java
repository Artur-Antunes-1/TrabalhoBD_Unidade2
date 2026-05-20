package modelo;

import java.time.LocalDate;

/**
 * Representa uma linha da tabela VENDE (uma venda no caixa).
 * Os itens da venda ficam em vende_produto e sao tratados a parte.
 */
public class Venda {
    private String    nfe;
    private LocalDate dataVenda;
    private String    pagamento;       // 'dinheiro','cartao','pix','boleto'
    private String    matriculaFunc;
    private String    cpfCliente;      // pode ser null

    public Venda() {}

    public Venda(String nfe, LocalDate dataVenda, String pagamento,
                 String matriculaFunc, String cpfCliente) {
        this.nfe           = nfe;
        this.dataVenda     = dataVenda;
        this.pagamento     = pagamento;
        this.matriculaFunc = matriculaFunc;
        this.cpfCliente    = cpfCliente;
    }

    public String    getNfe()                  { return nfe; }
    public void      setNfe(String v)          { this.nfe = v; }

    public LocalDate getDataVenda()            { return dataVenda; }
    public void      setDataVenda(LocalDate v) { this.dataVenda = v; }

    public String    getPagamento()            { return pagamento; }
    public void      setPagamento(String v)    { this.pagamento = v; }

    public String    getMatriculaFunc()        { return matriculaFunc; }
    public void      setMatriculaFunc(String v){ this.matriculaFunc = v; }

    public String    getCpfCliente()           { return cpfCliente; }
    public void      setCpfCliente(String v)   { this.cpfCliente = v; }
}
