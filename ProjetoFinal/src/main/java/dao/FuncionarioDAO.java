package dao;

import conexao.ConexaoBanco;
import modelo.Funcionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * FuncionarioDAO - acesso a dados da tabela FUNCIONARIO.
 *
 *  cuidado especial: "supervisor" e "telefone2" podem ser NULL.
 *  Por isso usamos setObject(... , Types.VARCHAR) ou tratamos com if.
 */
public class FuncionarioDAO {

    public void inserir(Funcionario funcionario) throws SQLException {
        String sql = "INSERT INTO funcionario " +
                     "(matricula, cpf, nome, telefone1, telefone2, tipo, cnpj_filial, supervisor) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, funcionario.getMatricula());
            ps.setString(2, funcionario.getCpf());
            ps.setString(3, funcionario.getNome());
            ps.setString(4, funcionario.getTelefone1());
            ps.setString(5, funcionario.getTelefone2());   // pode ser null
            ps.setString(6, funcionario.getTipo());
            ps.setString(7, funcionario.getCnpjFilial());
            ps.setString(8, funcionario.getSupervisor());  // pode ser null
            ps.executeUpdate();
        }
    }

    public List<Funcionario> listar() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT matricula, cpf, nome, telefone1, telefone2, tipo, " +
                     "       cnpj_filial, supervisor " +
                     "FROM   funcionario ORDER BY nome";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Funcionario(
                        rs.getString("matricula"),
                        rs.getString("cpf"),
                        rs.getString("nome"),
                        rs.getString("telefone1"),
                        rs.getString("telefone2"),
                        rs.getString("tipo"),
                        rs.getString("cnpj_filial"),
                        rs.getString("supervisor")
                ));
            }
        }
        return lista;
    }

    public void atualizar(Funcionario funcionario) throws SQLException {
        String sql = "UPDATE funcionario " +
                     "SET cpf = ?, nome = ?, telefone1 = ?, telefone2 = ?, " +
                     "    tipo = ?, cnpj_filial = ?, supervisor = ? " +
                     "WHERE matricula = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, funcionario.getCpf());
            ps.setString(2, funcionario.getNome());
            ps.setString(3, funcionario.getTelefone1());
            ps.setString(4, funcionario.getTelefone2());
            ps.setString(5, funcionario.getTipo());
            ps.setString(6, funcionario.getCnpjFilial());
            ps.setString(7, funcionario.getSupervisor());
            ps.setString(8, funcionario.getMatricula());
            ps.executeUpdate();
        }
    }

    public void excluir(String matricula) throws SQLException {
        String sql = "DELETE FROM funcionario WHERE matricula = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, matricula);
            ps.executeUpdate();
        }
    }
}
