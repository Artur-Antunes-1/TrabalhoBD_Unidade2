package dao;

import conexao.ConexaoBanco;
import modelo.LogAlteracao;
import modelo.ResumoFuncionario;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RelatoriosDAO
 * -------------------------------------------------------------------
 *  Reune o acesso a:
 *    - 4 consultas da Etapa 04
 *    - 2 views da Etapa 04
 *    - 2 funcoes  da Etapa 05
 *    - 2 procedimentos da Etapa 05
 *    - tabela de logs alimentada pelos triggers
 *    - tabela de resumo gerada pelo procedimento com cursor
 *    - consultas auxiliares para o dashboard
 *
 *  Cada metodo retorna um  List<Map<String,Object>>  (linha generica)
 *  para que a tela possa exibir o resultado em uma JTable sem precisar
 *  de uma classe modelo nova para cada relatorio.
 */
public class RelatoriosDAO {

    /* ======================================================================
     *                          CONSULTAS  (Etapa 04)
     * ====================================================================== */

    /** 1.1 - JOIN + GROUP BY + HAVING. */
    public List<Map<String,Object>> consultaFuncionariosComMaisDeUmaVenda() throws SQLException {
        String sql =
            "SELECT f.matricula, f.nome, COUNT(v.nfe) AS total_vendas " +
            "FROM   funcionario f " +
            "JOIN   vende       v ON v.matricula_func = f.matricula " +
            "GROUP BY f.matricula, f.nome " +
            "HAVING COUNT(v.nfe) > 1";
        return executarSelectGenerico(sql);
    }

    /** 1.2 - 2 JOINS + WHERE. */
    public List<Map<String,Object>> consultaVendasNoPeriodo(String dataIni, String dataFim) throws SQLException {
        String sql =
            "SELECT v.nfe, v.data_venda, c.nome AS cliente, f.nome AS funcionario " +
            "FROM   vende v " +
            "JOIN   cliente     c ON c.cpf       = v.cpf_cliente " +
            "JOIN   funcionario f ON f.matricula = v.matricula_func " +
            "WHERE  v.data_venda BETWEEN ? AND ? " +
            "ORDER BY v.data_venda";

        try (Connection con   = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dataIni);
            ps.setString(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                return lerResultSet(rs);
            }
        }
    }

    /** 1.3 - ANTI JOIN. */
    public List<Map<String,Object>> consultaClientesSemCompra() throws SQLException {
        String sql =
            "SELECT c.cpf, c.nome, c.telefone " +
            "FROM   cliente c " +
            "LEFT JOIN vende v ON v.cpf_cliente = c.cpf " +
            "WHERE  v.cpf_cliente IS NULL";
        return executarSelectGenerico(sql);
    }

    /** 1.4 - SUBCONSULTA. */
    public List<Map<String,Object>> consultaProdutosAcimaDaMedia() throws SQLException {
        String sql =
            "SELECT p.codigo, p.nome, p.preco_base " +
            "FROM   produto p " +
            "WHERE  p.preco_base > (SELECT AVG(preco_base) FROM produto)";
        return executarSelectGenerico(sql);
    }

    /* ======================================================================
     *                              VIEWS
     * ====================================================================== */

    /** Le a view vw_vendas_detalhadas. */
    public List<Map<String,Object>> viewVendasDetalhadas() throws SQLException {
        return executarSelectGenerico("SELECT * FROM vw_vendas_detalhadas");
    }

    /** Le a view vw_funcionarios_destaque. */
    public List<Map<String,Object>> viewFuncionariosDestaque() throws SQLException {
        return executarSelectGenerico("SELECT * FROM vw_funcionarios_destaque");
    }

    /* ======================================================================
     *                              FUNCOES
     * ====================================================================== */

    /**
     * Chama a function fn_total_venda(nfe).
     *
     *  No JDBC funcoes podem ser chamadas como uma consulta SELECT
     *  comum:  SELECT fn_total_venda(?) AS total
     */
    public BigDecimal chamarFuncaoTotalVenda(String nfe) throws SQLException {
        String sql = "SELECT fn_total_venda(?) AS total";
        try (Connection con   = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nfe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    /** Chama fn_categoria_funcionario(matricula). */
    public String chamarFuncaoCategoriaFuncionario(String matricula) throws SQLException {
        String sql = "SELECT fn_categoria_funcionario(?) AS categoria";
        try (Connection con   = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("categoria");
            }
        }
        return null;
    }

    /* ======================================================================
     *                          PROCEDIMENTOS
     * ====================================================================== */

    /**
     * Chama  pr_atualizar_preco_departamento(cod_dept, percentual)
     * usando CallableStatement (forma JDBC pura de chamar procedimento).
     */
    public void chamarProcedimentoAtualizarPreco(String codDepartamento,
                                                 BigDecimal percentual) throws SQLException {
        String sql = "{ CALL pr_atualizar_preco_departamento(?, ?) }";
        try (Connection con   = ConexaoBanco.obterConexao();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString    (1, codDepartamento);
            cs.setBigDecimal(2, percentual);
            cs.execute();
        }
    }

    /** Chama  pr_gerar_resumo_funcionarios()  (procedimento com CURSOR). */
    public void chamarProcedimentoGerarResumo() throws SQLException {
        String sql = "{ CALL pr_gerar_resumo_funcionarios() }";
        try (Connection con   = ConexaoBanco.obterConexao();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.execute();
        }
    }

    /* ======================================================================
     *               TABELAS ALIMENTADAS POR TRIGGER / PROCEDURE
     * ====================================================================== */

    /** Le os logs gerados por triggers (mais novos primeiro). */
    public List<LogAlteracao> listarLogs() throws SQLException {
        List<LogAlteracao> lista = new ArrayList<>();
        String sql = "SELECT id, tabela, tipo_evento, descricao, data_hora " +
                     "FROM   log_alteracoes ORDER BY data_hora DESC, id DESC";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new LogAlteracao(
                        rs.getInt("id"),
                        rs.getString("tabela"),
                        rs.getString("tipo_evento"),
                        rs.getString("descricao"),
                        rs.getTimestamp("data_hora").toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    /** Le a tabela de resumo gerada pelo procedimento com cursor. */
    public List<ResumoFuncionario> listarResumoFuncionarios() throws SQLException {
        List<ResumoFuncionario> lista = new ArrayList<>();
        String sql = "SELECT matricula, nome, total_vendas, valor_total, " +
                     "       classificacao, data_geracao " +
                     "FROM   resumo_vendas_funcionario " +
                     "ORDER BY total_vendas DESC, valor_total DESC";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ResumoFuncionario(
                        rs.getString("matricula"),
                        rs.getString("nome"),
                        rs.getInt("total_vendas"),
                        rs.getBigDecimal("valor_total"),
                        rs.getString("classificacao"),
                        rs.getTimestamp("data_geracao").toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    /* ======================================================================
     *                         CONSULTAS DO DASHBOARD
     * ====================================================================== */

    /** Quantidade de vendas por dia (para grafico de linha). */
    public Map<String,Integer> dashboardVendasPorDia() throws SQLException {
        Map<String,Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT data_venda, COUNT(*) AS qtd " +
                     "FROM   vende " +
                     "GROUP BY data_venda ORDER BY data_venda";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getDate("data_venda").toString(), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /** Quantidade de vendas por forma de pagamento (para grafico de pizza). */
    public Map<String,Integer> dashboardVendasPorPagamento() throws SQLException {
        Map<String,Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT pagamento, COUNT(*) AS qtd " +
                     "FROM   vende GROUP BY pagamento";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getString("pagamento"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /** Top 5 produtos mais vendidos (em quantidade). */
    public Map<String,Integer> dashboardTopProdutos() throws SQLException {
        Map<String,Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT p.nome, SUM(vp.quantidade) AS total " +
                     "FROM   vende_produto vp " +
                     "JOIN   produto p ON p.codigo = vp.cod_produto " +
                     "GROUP BY p.nome ORDER BY total DESC LIMIT 5";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getString("nome"), rs.getInt("total"));
            }
        }
        return resultado;
    }

    /** Vendas por funcionario (barras). */
    public Map<String,Integer> dashboardVendasPorFuncionario() throws SQLException {
        Map<String,Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT f.nome, COUNT(v.nfe) AS qtd " +
                     "FROM   funcionario f " +
                     "LEFT JOIN vende v ON v.matricula_func = f.matricula " +
                     "GROUP BY f.nome HAVING qtd > 0 ORDER BY qtd DESC LIMIT 10";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getString("nome"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /** Vendas por filial (atraves do funcionario que registrou). */
    public Map<String,Integer> dashboardVendasPorFilial() throws SQLException {
        Map<String,Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT fi.cnpj, COUNT(v.nfe) AS qtd " +
                     "FROM   filial fi " +
                     "LEFT JOIN funcionario fu ON fu.cnpj_filial = fi.cnpj " +
                     "LEFT JOIN vende       v  ON v.matricula_func = fu.matricula " +
                     "GROUP BY fi.cnpj HAVING qtd > 0";

        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getString("cnpj"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /* ----------------------- INDICADORES NUMERICOS ----------------------- */

    public int contarRegistros(String tabela) throws SQLException {
        String sql = "SELECT COUNT(*) AS qtd FROM " + tabela;   // tabela e fixa
        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("qtd");
        }
        return 0;
    }

    public BigDecimal mediaPrecoProdutos() throws SQLException {
        String sql = "SELECT AVG(preco_base) AS m FROM produto";
        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal valor = rs.getBigDecimal("m");
                return (valor != null) ? valor.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /* ======================================================================
     *                          UTILITARIOS PRIVADOS
     * ====================================================================== */

    /** Executa um SELECT sem parametros e devolve linhas como List<Map>. */
    private List<Map<String,Object>> executarSelectGenerico(String sql) throws SQLException {
        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery(sql)) {
            return lerResultSet(rs);
        }
    }

    /** Le um ResultSet em uma estrutura List<Map> independente das colunas. */
    private List<Map<String,Object>> lerResultSet(ResultSet rs) throws SQLException {
        List<Map<String,Object>> linhas = new ArrayList<>();
        int qtdCols = rs.getMetaData().getColumnCount();

        while (rs.next()) {
            Map<String,Object> linha = new LinkedHashMap<>();
            for (int i = 1; i <= qtdCols; i++) {
                linha.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            linhas.add(linha);
        }
        return linhas;
    }
}
