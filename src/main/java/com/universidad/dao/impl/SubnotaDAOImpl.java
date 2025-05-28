package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.universidad.dao.SubnotaDAO;
import com.universidad.model.Subnota;
import com.universidad.util.DatabaseConnection;

public class SubnotaDAOImpl implements SubnotaDAO {
    @Override
    public Subnota create(Subnota subnota) throws Exception {
        String sql = "INSERT INTO Subnota (id_calificacion, parcial, numero, valor) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, subnota.getIdCalificacion());
            pstmt.setInt(2, subnota.getParcial());
            pstmt.setInt(3, subnota.getNumero());
            pstmt.setDouble(4, subnota.getValor());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating subnota failed, no rows affected.");
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subnota.setIdSubnota(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating subnota failed, no ID obtained.");
                }
            }
            return subnota;
        }
    }

    @Override
    public Subnota read(Integer id) throws Exception {
        String sql = "SELECT * FROM Subnota WHERE id_subnota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSubnota(rs);
            }
            return null;
        }
    }

    @Override
    public List<Subnota> findByCalificacion(Integer idCalificacion) throws Exception {
        String sql = "SELECT * FROM Subnota WHERE id_calificacion = ? ORDER BY parcial, numero";
        List<Subnota> subnotas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCalificacion);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subnotas.add(mapResultSetToSubnota(rs));
            }
            return subnotas;
        }
    }

    @Override
    public Subnota update(Subnota subnota) throws Exception {
        String sql = "UPDATE Subnota SET parcial = ?, numero = ?, valor = ? WHERE id_subnota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subnota.getParcial());
            pstmt.setInt(2, subnota.getNumero());
            pstmt.setDouble(3, subnota.getValor());
            pstmt.setInt(4, subnota.getIdSubnota());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Updating subnota failed, no rows affected.");
            return subnota;
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM Subnota WHERE id_subnota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Deleting subnota failed, no rows affected.");
        }
    }

    private Subnota mapResultSetToSubnota(ResultSet rs) throws SQLException {
        Subnota subnota = new Subnota();
        subnota.setIdSubnota(rs.getInt("id_subnota"));
        subnota.setIdCalificacion(rs.getInt("id_calificacion"));
        subnota.setParcial(rs.getInt("parcial"));
        subnota.setNumero(rs.getInt("numero"));
        subnota.setValor(rs.getDouble("valor"));
        return subnota;
    }
} 