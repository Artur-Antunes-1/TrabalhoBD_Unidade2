package dao;

import conexao.ConexaoBanco;
import modelo.DepartamentoProduto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartamentoProdutoDAO - acesso a dados da tabela DEPARTAMENTO_PRODUTO.
 *
 *  Tabela de associacao N:N.  Como as duas colunas formam a PK composta,
 *  nao expomos UPDATE - so faz sentido inserir uma nova associacao ou
 *  excluir uma existente.
 */
public class DepartamentoProdutoDAO {

    /** INSERT - associa um produto a um departamento. */
    public void inserir(DepartamentoProduto dp) throws SQLException {
        String sql = "INSERT INTO departamento_produto (cod_departamento, cod_produto) " +
                     "VALUES (?, ?)";

        try (Connection conexao    = ConexaoBanco.obterConexao();
             PreparedStatement ps  = conexao.prepareStatement(sql)) {

            ps.setString(1, dp.getCodDepartamento());
            ps.setString(2, dp.getCodProduto());
            ps.executeUpdate();
        }
    }

    /** SELECT - lista todas as associacoes. */
    public List<DepartamentoProduto> listar() throws SQLException {
        List<DepartamentoProduto> lista = new ArrayList<>();
        String sql = "SELECT cod_departamento, cod_produto " +
                     "FROM   departamento_produto " +
                     "ORDER BY cod_departamento, cod_produto";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new DepartamentoProduto(
                        rs.getString("cod_departamento"),
                        rs.getString("cod_produto")
                ));
            }
        }
        return lista;
    }

    /** DELETE - remove uma associacao pela PK composta. */
    public void excluir(String codDepartamento, String codProduto) throws SQLException {
        String sql = "DELETE FROM departamento_produto " +
                     "WHERE  cod_departamento = ? AND cod_produto = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, codDepartamento);
            ps.setString(2, codProduto);
            ps.executeUpdate();
        }
    }
}
