package dao;

import conexao.ConexaoBanco;
import modelo.Departamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartamentoDAO - acesso a dados da tabela DEPARTAMENTO.
 * Segue o mesmo padrao das demais DAOs (JDBC + PreparedStatement,
 * SQL escrita a mao, sem ORM).
 */
public class DepartamentoDAO {

    /** INSERT - cadastra um novo departamento. */
    public void inserir(Departamento d) throws SQLException {
        String sql = "INSERT INTO departamento (codigo, categoria) VALUES (?, ?)";

        try (Connection conexao    = ConexaoBanco.obterConexao();
             PreparedStatement ps  = conexao.prepareStatement(sql)) {

            ps.setString(1, d.getCodigo());
            ps.setString(2, d.getCategoria());
            ps.executeUpdate();
        }
    }

    /** SELECT - lista todos os departamentos. */
    public List<Departamento> listar() throws SQLException {
        List<Departamento> lista = new ArrayList<>();
        String sql = "SELECT codigo, categoria FROM departamento ORDER BY codigo";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Departamento(
                        rs.getString("codigo"),
                        rs.getString("categoria")
                ));
            }
        }
        return lista;
    }

    /** UPDATE - atualiza a categoria do departamento identificado pelo codigo. */
    public void atualizar(Departamento d) throws SQLException {
        String sql = "UPDATE departamento SET categoria = ? WHERE codigo = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, d.getCategoria());
            ps.setString(2, d.getCodigo());
            ps.executeUpdate();
        }
    }

    /** DELETE - remove um departamento pelo codigo. */
    public void excluir(String codigo) throws SQLException {
        String sql = "DELETE FROM departamento WHERE codigo = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }
}
