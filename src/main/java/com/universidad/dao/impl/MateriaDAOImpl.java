package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.universidad.dao.MateriaDAO;
import com.universidad.model.Materia;
import com.universidad.util.DatabaseConnection;

public class MateriaDAOImpl implements MateriaDAO {
    
    @Override
    public Materia create(Materia materia) throws Exception {
        String sql = "INSERT INTO Materia (nombre_materia) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, materia.getNombre_materia());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating materia failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    materia.setIdMateria(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating materia failed, no ID obtained.");
                }
            } catch (SQLException e) {
                 throw new SQLException("Error retrieving generated key: " + e.getMessage());
            }
            return materia;
        }
    }
    
    @Override
    public Materia read(Integer id) throws Exception {
        String sql = "SELECT * FROM Materia WHERE id_materia = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMateria(rs);
            }
            
            return null;
        }
    }
    
    @Override
    public List<Materia> readAll() throws Exception {
        String sql = "SELECT * FROM Materia";
        
        List<Materia> materias = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                materias.add(mapResultSetToMateria(rs));
            }
            
            return materias;
        }
    }
    
    @Override
    public Materia update(Materia materia) throws Exception {
        String sql = "UPDATE Materia SET nombre_materia = ? WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, materia.getNombre_materia());
            pstmt.setInt(2, materia.getIdMateria());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating materia failed, no rows affected.");
            }
            return materia;
        }
    }
    
    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM Materia WHERE id_materia = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting materia failed, no rows affected.");
            }
        }        
    }
    
    @Override
    public List<Materia> findByCarrera(Integer idCarrera) throws Exception {
        // Ya no existe la relación con Carrera, así que devuelvo todas las materias
        return readAll();
    }
    
    private Materia mapResultSetToMateria(ResultSet rs) throws SQLException {
        Materia materia = new Materia();
        materia.setIdMateria(rs.getInt("id_materia"));
        materia.setNombre_materia(rs.getString("nombre_materia"));
        return materia;
    }
} 