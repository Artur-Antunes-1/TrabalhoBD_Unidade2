package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ConexaoBanco
 * -------------------------------------------------------------------
 *  UNICO ponto da aplicacao que abre conexao com o MySQL.
 *  Toda classe DAO chama  ConexaoBanco.obterConexao()  para
 *  conversar com o banco.
 *
 *  Como funciona (passo a passo):
 *    1. DriverManager   -  classe do JDBC (java.sql) que sabe
 *                          encontrar o driver instalado no classpath.
 *    2. mysql-connector-j -  e o driver JDBC do MySQL.  Esta declarado
 *                          no  pom.xml  como dependencia (e a UNICA
 *                          biblioteca relacionada a banco).
 *    3. URL_BANCO        -  endereco do banco no formato JDBC:
 *                            jdbc:mysql://HOST:PORTA/NOME_DO_BANCO
 *    4. USUARIO/SENHA    -  credenciais do MySQL local.
 *
 *  Importante:
 *    - Nao usamos NENHUM ORM (Hibernate, JPA, etc).
 *    - Cada DAO usa  PreparedStatement  com SQL escrito a mao.
 *    - O metodo retorna a conexao "crua" do JDBC para que o DAO
 *      possa criar PreparedStatement, executar SELECT/INSERT/UPDATE/
 *      DELETE e ler ResultSet.
 */
public class ConexaoBanco {

    // Endereco do banco MySQL local.
    private static final String URL_BANCO = "jdbc:mysql://localhost:3306/supermercado";
    // Usuario do MySQL.  Ajuste se o seu for diferente.
    private static final String USUARIO   = "root";
    // Senha do MySQL.  Ajuste se a sua for diferente.
    private static final String SENHA     = "root";

    /**
     * Abre uma nova conexao com o banco e retorna o objeto Connection.
     * O DAO que chama esse metodo e responsavel por fechar a conexao
     * (usamos try-with-resources para isso).
     */
    public static Connection obterConexao() throws SQLException {
        return DriverManager.getConnection(URL_BANCO, USUARIO, SENHA);
    }
}
