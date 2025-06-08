package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.universidad.dao.EstudianteDAO;
import com.universidad.model.Estudiante;
import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

public class EstudianteDAOImpl implements EstudianteDAO {
    
    @Override
    public Estudiante create(Estudiante estudiante) throws Exception {
        String sql = "INSERT INTO Estudiante (id_usuario) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, estudiante.getUsuario().getIdUsuario());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating estudiante failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    estudiante.setIdEstudiante(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating estudiante failed, no ID obtained.");
                }
            }
            return estudiante;
        }
    }
    
    @Override
    public Estudiante read(Integer id) throws Exception {
        String sql = "SELECT e.*, u.* FROM Estudiante e " +
                    "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                    "WHERE e.id_estudiante = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEstudiante(rs);
            }
            
            return null;
        }
    }
    
    @Override
    public List<Estudiante> readAll() throws Exception {
        String sql = "SELECT e.*, u.* FROM Estudiante e " +
                    "JOIN Usuario u ON e.id_usuario = u.id_usuario";
        
        List<Estudiante> estudiantes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                estudiantes.add(mapResultSetToEstudiante(rs));
            }
            
            return estudiantes;
        }
    }
    
    @Override
    public Estudiante update(Estudiante estudiante) throws Exception {
        String sql = "UPDATE Estudiante SET id_usuario = ? WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, estudiante.getUsuario().getIdUsuario());
            pstmt.setInt(2, estudiante.getIdEstudiante());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating estudiante failed, no rows affected.");
            }
            return estudiante;
        }
    }
    
    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM Estudiante WHERE id_estudiante = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting estudiante failed, no rows affected.");
            }
        }
    }
    
    @Override
    public List<Estudiante> findByCarrera(Integer idCarrera) throws Exception {
        // Ya no existe la relación con Carrera, así que devuelvo todos los estudiantes
        return readAll();
    }
    
    private Estudiante mapResultSetToEstudiante(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCedula(rs.getString("cedula"));
        usuario.setNombre_usuario(rs.getString("nombre_usuario"));
        usuario.setApellido_usuario(rs.getString("apellido_usuario"));
        usuario.setPassword(rs.getString("password"));
        usuario.setRol(rs.getString("rol"));
        
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(rs.getInt("id_estudiante"));
        estudiante.setUsuario(usuario);
        
        return estudiante;
    }
} 