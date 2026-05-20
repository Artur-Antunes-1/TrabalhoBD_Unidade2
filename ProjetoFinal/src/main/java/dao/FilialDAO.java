package dao;

import conexao.ConexaoBanco;
import modelo.Filial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * FilialDAO - acesso a dados da tabela FILIAL.
 */
public class FilialDAO {

    public void inserir(Filial filial) throws SQLException {
        String sql = "INSERT INTO filial (cnpj, rua, numero, cep) VALUES (?, ?, ?, ?)";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, filial.getCnpj());
            ps.setString(2, filial.getRua());
            ps.setString(3, filial.getNumero());
            ps.setString(4, filial.getCep());
            ps.executeUpdate();
        }
    }

    public List<Filial> listar() throws SQLException {
        List<Filial> lista = new ArrayList<>();
        String sql = "SELECT cnpj, rua, numero, cep FROM filial ORDER BY cnpj";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Filial(
                        rs.getString("cnpj"),
                        rs.getString("rua"),
                        rs.getString("numero"),
                        rs.getString("cep")
                ));
            }
        }
        return lista;
    }

    public void atualizar(Filial filial) throws SQLException {
        String sql = "UPDATE filial SET rua = ?, numero = ?, cep = ? WHERE cnpj = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, filial.getRua());
            ps.setString(2, filial.getNumero());
            ps.setString(3, filial.getCep());
            ps.setString(4, filial.getCnpj());
            ps.executeUpdate();
        }
    }

    public void excluir(String cnpj) throws SQLException {
        String sql = "DELETE FROM filial WHERE cnpj = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, cnpj);
            ps.executeUpdate();
        }
    }
}
