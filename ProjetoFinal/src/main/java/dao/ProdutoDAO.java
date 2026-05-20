package dao;

import conexao.ConexaoBanco;
import modelo.Produto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ProdutoDAO - acesso a dados da tabela PRODUTO.
 * Mesmo padrao do ClienteDAO.
 */
public class ProdutoDAO {

    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (codigo, nome, preco_base) VALUES (?, ?, ?)";

        try (Connection conexao    = ConexaoBanco.obterConexao();
             PreparedStatement ps  = conexao.prepareStatement(sql)) {

            ps.setString    (1, produto.getCodigo());
            ps.setString    (2, produto.getNome());
            ps.setBigDecimal(3, produto.getPrecoBase());
            ps.executeUpdate();
        }
    }

    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT codigo, nome, preco_base FROM produto ORDER BY nome";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Produto(
                        rs.getString("codigo"),
                        rs.getString("nome"),
                        rs.getBigDecimal("preco_base")
                ));
            }
        }
        return lista;
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, preco_base = ? WHERE codigo = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString    (1, produto.getNome());
            ps.setBigDecimal(2, produto.getPrecoBase());
            ps.setString    (3, produto.getCodigo());
            ps.executeUpdate();
        }
    }

    public void excluir(String codigo) throws SQLException {
        String sql = "DELETE FROM produto WHERE codigo = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    /**
     * UPDATE direto de preco. Util para demonstrar o trigger
     * tg_log_alteracao_preco (que registra a alteracao em log_alteracoes).
     */
    public void alterarPreco(String codigo, BigDecimal novoPreco) throws SQLException {
        String sql = "UPDATE produto SET preco_base = ? WHERE codigo = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setBigDecimal(1, novoPreco);
            ps.setString    (2, codigo);
            ps.executeUpdate();
        }
    }
}
