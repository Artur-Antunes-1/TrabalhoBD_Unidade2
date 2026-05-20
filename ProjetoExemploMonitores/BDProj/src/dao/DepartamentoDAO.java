package dao;

import model.Departamento;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDAO {
    public void inserir(Departamento d) throws SQLException {
        String sql = "INSERT INTO Departamento (nome) VALUES (?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNome());
            ps.executeUpdate();
        }
    }

    public List<Departamento> listar() throws SQLException {
        List<Departamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Departamento";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Departamento(rs.getInt("coddepto"), rs.getString("nome")));
            }
        }
        return lista;
    }

    public void atualizar(Departamento d) throws SQLException {
        String sql = "UPDATE Departamento SET nome=? WHERE coddepto=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNome());
            ps.setInt(2, d.getCoddepto());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM Departamento WHERE coddepto=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}

