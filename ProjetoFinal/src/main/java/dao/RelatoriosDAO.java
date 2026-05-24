package dao;

import conexao.ConexaoBanco;
import modelo.LogAlteracao;

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

    /**
     * Chama fn_porte_venda(nfe).
     *
     *  Retorna "GRANDE", "MEDIA" ou "PEQUENA" conforme o valor total
     *  da venda (a funcao reaproveita fn_total_venda internamente).
     */
    public String chamarFuncaoPorteVenda(String nfe) throws SQLException {
        String sql = "SELECT fn_porte_venda(?) AS porte";
        try (Connection con   = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nfe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("porte");
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

    /**
     * Chama pr_promocao_produtos_parados(perc_sem_vendas, perc_pouca_venda)
     * (procedimento com CURSOR).
     *
     *  Percorre todos os produtos e aplica desconto:
     *   - produtos sem nenhuma venda  ->  desconto perc_sem_vendas
     *   - produtos com ate 2 unidades vendidas  ->  desconto perc_pouca_venda
     */
    public void chamarProcedimentoPromocaoProdutosParados(BigDecimal percSemVendas,
                                                          BigDecimal percPoucaVenda) throws SQLException {
        String sql = "{ CALL pr_promocao_produtos_parados(?, ?) }";
        try (Connection con   = ConexaoBanco.obterConexao();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setBigDecimal(1, percSemVendas);
            cs.setBigDecimal(2, percPoucaVenda);
            cs.execute();
        }
    }

    /* ======================================================================
     *                  TABELAS ALIMENTADAS POR TRIGGER
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

    /* ======================================================================
     *                         CONSULTAS DO DASHBOARD
     *
     *  Os 5 graficos aceitam 4 filtros opcionais (qualquer um pode ser
     *  null = sem filtro):
     *    - dataIni / dataFim ("yyyy-MM-dd")
     *    - cnpjFilial
     *    - pagamento
     *  Quando a propria dimensao do grafico e um dos filtros (ex: filial),
     *  o filtro correspondente NAO e aplicado para evitar zerar a serie.
     * ====================================================================== */

    /** Lista os CNPJs das filiais para popular o combo de filtro. */
    public List<String> listarCnpjsFiliais() throws SQLException {
        List<String> lista = new ArrayList<>();
        try (Connection con = ConexaoBanco.obterConexao();
             Statement   st = con.createStatement();
             ResultSet   rs = st.executeQuery("SELECT cnpj FROM filial ORDER BY cnpj")) {
            while (rs.next()) lista.add(rs.getString("cnpj"));
        }
        return lista;
    }

    /** Quantidade de vendas considerando os filtros (para indicador resumido). */
    public int contarVendasFiltradas(String dataIni, String dataFim,
                                     String cnpjFilial, String pagamento) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) AS qtd FROM vende v " +
            "LEFT JOIN funcionario f ON f.matricula = v.matricula_func");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, cnpjFilial, pagamento);

        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("qtd");
            }
        }
        return 0;
    }

    /** Vendas por dia (linha) com filtros. */
    public Map<String,Integer> dashboardVendasPorDia(String dataIni, String dataFim,
                                                     String cnpjFilial, String pagamento) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT v.data_venda, COUNT(*) AS qtd FROM vende v " +
            "LEFT JOIN funcionario f ON f.matricula = v.matricula_func");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, cnpjFilial, pagamento);
        sql.append(" GROUP BY v.data_venda ORDER BY v.data_venda");

        Map<String,Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getDate("data_venda").toString(), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /**
     * Vendas por forma de pagamento (pizza).
     * Nao filtra por pagamento (essa e a dimensao do grafico).
     */
    public Map<String,Integer> dashboardVendasPorPagamento(String dataIni, String dataFim,
                                                            String cnpjFilial) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT v.pagamento, COUNT(*) AS qtd FROM vende v " +
            "LEFT JOIN funcionario f ON f.matricula = v.matricula_func");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, cnpjFilial, null);
        sql.append(" GROUP BY v.pagamento");

        Map<String,Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString("pagamento"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /** Top 5 produtos mais vendidos com filtros. */
    public Map<String,Integer> dashboardTopProdutos(String dataIni, String dataFim,
                                                    String cnpjFilial, String pagamento) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT p.nome, SUM(vp.quantidade) AS total " +
            "FROM vende_produto vp " +
            "JOIN produto p ON p.codigo = vp.cod_produto " +
            "JOIN vende   v ON v.nfe    = vp.nfe " +
            "LEFT JOIN funcionario f ON f.matricula = v.matricula_func");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, cnpjFilial, pagamento);
        sql.append(" GROUP BY p.nome ORDER BY total DESC LIMIT 5");

        Map<String,Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString("nome"), rs.getInt("total"));
            }
        }
        return resultado;
    }

    /** Vendas por funcionario (barras) com filtros. */
    public Map<String,Integer> dashboardVendasPorFuncionario(String dataIni, String dataFim,
                                                              String cnpjFilial, String pagamento) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT f.nome, COUNT(v.nfe) AS qtd " +
            "FROM funcionario f " +
            "JOIN vende v ON v.matricula_func = f.matricula");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, cnpjFilial, pagamento);
        sql.append(" GROUP BY f.nome HAVING COUNT(v.nfe) > 0 ORDER BY COUNT(v.nfe) DESC LIMIT 10");

        Map<String,Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString("nome"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /**
     * Vendas por filial (barras) com filtros.
     * Nao filtra por filial (essa e a dimensao do grafico).
     */
    public Map<String,Integer> dashboardVendasPorFilial(String dataIni, String dataFim,
                                                        String pagamento) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT fi.cnpj, COUNT(v.nfe) AS qtd " +
            "FROM filial fi " +
            "LEFT JOIN funcionario f ON f.cnpj_filial = fi.cnpj " +
            "LEFT JOIN vende       v ON v.matricula_func = f.matricula");
        List<Object> params = new ArrayList<>();
        aplicarFiltrosVende(sql, params, dataIni, dataFim, null, pagamento);
        sql.append(" GROUP BY fi.cnpj HAVING COUNT(v.nfe) > 0");

        Map<String,Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexaoBanco.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString("cnpj"), rs.getInt("qtd"));
            }
        }
        return resultado;
    }

    /** Helper privado: monta as clausulas WHERE conforme filtros nao-nulos. */
    private void aplicarFiltrosVende(StringBuilder sql, List<Object> params,
                                     String dataIni, String dataFim,
                                     String cnpjFilial, String pagamento) {
        List<String> wheres = new ArrayList<>();
        if (dataIni != null && dataFim != null) {
            wheres.add("v.data_venda BETWEEN ? AND ?");
            params.add(dataIni);
            params.add(dataFim);
        }
        if (cnpjFilial != null) {
            wheres.add("f.cnpj_filial = ?");
            params.add(cnpjFilial);
        }
        if (pagamento != null) {
            wheres.add("v.pagamento = ?");
            params.add(pagamento);
        }
        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }
    }

    /** Helper privado: aplica os parametros posicionais no PreparedStatement. */
    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
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
