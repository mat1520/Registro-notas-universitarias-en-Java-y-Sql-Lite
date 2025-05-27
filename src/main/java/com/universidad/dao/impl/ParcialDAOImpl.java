package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.universidad.dao.ParcialDAO;
import com.universidad.model.Parcial;
import com.universidad.util.DatabaseConnection;

public class ParcialDAOImpl implements ParcialDAO {
    @Override
    public List<Parcial> findAll() throws Exception {
        String sql = "SELECT * FROM Parcial ORDER BY id_parcial";
        List<Parcial> parciales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                parciales.add(new Parcial(
                    rs.getInt("id_parcial"),
                    rs.getString("nombre"),
                    rs.getDouble("porcentaje")
                ));
            }
        }
        return parciales;
    }

    @Override
    public Parcial findById(int id) throws Exception {
        String sql = "SELECT * FROM Parcial WHERE id_parcial = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Parcial(
                    rs.getInt("id_parcial"),
                    rs.getString("nombre"),
                    rs.getDouble("porcentaje")
                );
            }
        }
        return null;
    }
} 