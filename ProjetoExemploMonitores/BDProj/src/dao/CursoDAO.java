package dao;

import model.Curso;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {
    public void inserir(Curso c) throws SQLException {
        String sql = "INSERT INTO Curso (codcurso, nome, descricao, coddepto) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getCodcurso());
            ps.setString(2, c.getNome());
            ps.setString(3, c.getDescricao());
            ps.setInt(4, c.getCoddepto());
            ps.executeUpdate();
        }
    }

    public List<Curso> listar() throws SQLException {
        List<Curso> lista = new ArrayList<>();
        String sql = "SELECT * FROM Curso";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Curso(
                    rs.getInt("codcurso"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getInt("coddepto")
                ));
            }
        }
        return lista;
    }

    public void atualizar(Curso c) throws SQLException {
        String sql = "UPDATE Curso SET nome=?, descricao=?, coddepto=? WHERE codcurso=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getDescricao());
            ps.setInt(3, c.getCoddepto());
            ps.setInt(4, c.getCodcurso());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Curso WHERE codcurso=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
