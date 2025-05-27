package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.universidad.dao.SubnotaDAO;
import com.universidad.model.Subnota;
import com.universidad.util.DatabaseConnection;

public class SubnotaDAOImpl implements SubnotaDAO {
    @Override
    public List<Subnota> findByCalificacion(int idCalificacion) throws Exception {
        String sql = "SELECT * FROM Subnota WHERE id_calificacion = ? ORDER BY id_parcial, numero";
        List<Subnota> subnotas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCalificacion);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subnotas.add(new Subnota(
                    rs.getInt("id_subnota"),
                    rs.getInt("id_calificacion"),
                    rs.getInt("id_parcial"),
                    rs.getInt("numero"),
                    rs.getDouble("valor")
                ));
            }
        }
        return subnotas;
    }

    @Override
    public List<Subnota> findByCalificacionAndParcial(int idCalificacion, int idParcial) throws Exception {
        String sql = "SELECT * FROM Subnota WHERE id_calificacion = ? AND id_parcial = ? ORDER BY numero";
        List<Subnota> subnotas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCalificacion);
            pstmt.setInt(2, idParcial);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subnotas.add(new Subnota(
                    rs.getInt("id_subnota"),
                    rs.getInt("id_calificacion"),
                    rs.getInt("id_parcial"),
                    rs.getInt("numero"),
                    rs.getDouble("valor")
                ));
            }
        }
        return subnotas;
    }

    @Override
    public void save(Subnota subnota) throws Exception {
        String sql = "INSERT OR REPLACE INTO Subnota (id_subnota, id_calificacion, id_parcial, numero, valor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, subnota.getIdSubnota());
            pstmt.setInt(2, subnota.getIdCalificacion());
            pstmt.setInt(3, subnota.getIdParcial());
            pstmt.setInt(4, subnota.getNumero());
            pstmt.setDouble(5, subnota.getValor());
            pstmt.executeUpdate();
        }
    }
} 