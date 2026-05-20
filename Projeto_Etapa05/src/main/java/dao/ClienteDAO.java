package dao;

import conexao.ConexaoBanco;
import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ClienteDAO - acesso a dados da tabela CLIENTE.
 *
 *  Padrao usado em TODAS as DAOs:
 *
 *    1) Escrevemos a SQL como String  (visivel, explicita, sem ORM).
 *    2) Pedimos uma conexao para  ConexaoBanco.obterConexao().
 *    3) Criamos um  PreparedStatement  com a SQL.
 *    4) Setamos os parametros (?) com setString/setInt/etc.
 *    5) Chamamos  executeUpdate()  ou  executeQuery().
 *    6) Lemos o ResultSet (em SELECTs) e populamos objetos do modelo.
 *    7) try-with-resources fecha tudo automaticamente.
 *
 *  Esse padrao se repete em ProdutoDAO, FilialDAO, FuncionarioDAO,
 *  VendaDAO, RelatoriosDAO etc.
 */
public class ClienteDAO {

    /** INSERT - cadastra um novo cliente. */
    public void inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (cpf, nome, telefone) VALUES (?, ?, ?)";

        try (Connection conexao    = ConexaoBanco.obterConexao();
             PreparedStatement ps  = conexao.prepareStatement(sql)) {

            ps.setString(1, cliente.getCpf());
            ps.setString(2, cliente.getNome());
            ps.setString(3, cliente.getTelefone());
            ps.executeUpdate();
        }
    }

    /** SELECT - lista todos os clientes. */
    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT cpf, nome, telefone FROM cliente ORDER BY nome";

        try (Connection conexao = ConexaoBanco.obterConexao();
             Statement   st     = conexao.createStatement();
             ResultSet   rs     = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getString("cpf"),
                        rs.getString("nome"),
                        rs.getString("telefone")
                ));
            }
        }
        return lista;
    }

    /** UPDATE - atualiza nome/telefone do cliente identificado pelo CPF. */
    public void atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, telefone = ? WHERE cpf = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getTelefone());
            ps.setString(3, cliente.getCpf());
            ps.executeUpdate();
        }
    }

    /** DELETE - remove um cliente pelo CPF. */
    public void excluir(String cpf) throws SQLException {
        String sql = "DELETE FROM cliente WHERE cpf = ?";

        try (Connection conexao   = ConexaoBanco.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, cpf);
            ps.executeUpdate();
        }
    }
}
