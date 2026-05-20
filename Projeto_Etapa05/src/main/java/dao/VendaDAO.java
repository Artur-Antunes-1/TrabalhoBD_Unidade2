package dao;

import conexao.ConexaoBanco;
import modelo.ItemVenda;
import modelo.Venda;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * VendaDAO - acesso a dados da tabela VENDE e VENDE_PRODUTO.
 *
 *  A venda em si e simples (1 linha em VENDE). O detalhe esta nos
 *  itens da venda (1+ linhas em VENDE_PRODUTO).  Por isso esse DAO
 *  tem dois grupos de metodos:
 *
 *    - venda: inserir, listar, atualizar, excluir
 *    - itens da venda: inserirItem, listarItensDaVenda, removerItens
 */
public class VendaDAO {

    /* =========================  VENDA  ============================= */

    public void inserir(Venda venda) throws SQLException {
        String sql = "INSERT INTO vende " +
                     "(nfe, data_venda, pagamento, matricula_func, cpf_cliente) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, venda.getNfe());
            ps.setDate  (2, Date.valueOf(venda.getDataVenda()));
            ps.setString(3, venda.getPagamento());
            ps.setString(4, venda.getMatriculaFunc());
            ps.setString(5, venda.getCpfCliente());   // pode ser null
            ps.executeUpdate();
        }
    }

    public List<Venda> listar() throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT nfe, data_venda, pagamento, matricula_func, cpf_cliente " +
                     "FROM   vende ORDER BY data_venda DESC";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Venda(
                        rs.getString("nfe"),
                        rs.getDate("data_venda").toLocalDate(),
                        rs.getString("pagamento"),
                        rs.getString("matricula_func"),
                        rs.getString("cpf_cliente")
                ));
            }
        }
        return lista;
    }

    public void atualizar(Venda venda) throws SQLException {
        String sql = "UPDATE vende " +
                     "SET data_venda = ?, pagamento = ?, " +
                     "    matricula_func = ?, cpf_cliente = ? " +
                     "WHERE nfe = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate  (1, Date.valueOf(venda.getDataVenda()));
            ps.setString(2, venda.getPagamento());
            ps.setString(3, venda.getMatriculaFunc());
            ps.setString(4, venda.getCpfCliente());
            ps.setString(5, venda.getNfe());
            ps.executeUpdate();
        }
    }

    public void excluir(String nfe) throws SQLException {
        String sql = "DELETE FROM vende WHERE nfe = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, nfe);
            ps.executeUpdate();
        }
    }

    /* =====================  ITENS DA VENDA  ======================== */

    public void inserirItem(ItemVenda item) throws SQLException {
        String sql = "INSERT INTO vende_produto (nfe, cod_produto, quantidade) " +
                     "VALUES (?, ?, ?)";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, item.getNfe());
            ps.setString(2, item.getCodProduto());
            ps.setInt   (3, item.getQuantidade());
            ps.executeUpdate();
        }
    }

    public List<ItemVenda> listarItensDaVenda(String nfe) throws SQLException {
        List<ItemVenda> lista = new ArrayList<>();
        String sql = "SELECT nfe, cod_produto, quantidade " +
                     "FROM   vende_produto WHERE nfe = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, nfe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ItemVenda(
                            rs.getString("nfe"),
                            rs.getString("cod_produto"),
                            rs.getInt("quantidade")
                    ));
                }
            }
        }
        return lista;
    }
}
