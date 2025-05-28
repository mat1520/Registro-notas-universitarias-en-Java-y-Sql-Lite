package com.universidad.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.universidad.dao.ProfesorDAO;
import com.universidad.model.Profesor;
import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

public class ProfesorDAOImpl implements ProfesorDAO {
    
    @Override
    public Profesor create(Profesor profesor) throws Exception {
        String sql = "INSERT INTO Profesor (id_usuario) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, profesor.getUsuario().getIdUsuario());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating profesor failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profesor.setIdProfesor(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating profesor failed, no ID obtained.");
                }
            }
            return profesor;
        }
    }
    
    @Override
    public Profesor read(Integer id) throws Exception {
        String sql = "SELECT p.*, u.* FROM Profesor p " +
                    "JOIN Usuario u ON p.id_usuario = u.id_usuario " +
                    "WHERE p.id_profesor = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProfesor(rs);
            }
            
            return null;
        }
    }
    
    @Override
    public List<Profesor> readAll() throws Exception {
        String sql = "SELECT p.*, u.* FROM Profesor p " +
                    "JOIN Usuario u ON p.id_usuario = u.id_usuario";
        
        List<Profesor> profesores = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                profesores.add(mapResultSetToProfesor(rs));
            }
            
            return profesores;
        }
    }
    
    @Override
    public Profesor update(Profesor profesor) throws Exception {
        String sql = "UPDATE Profesor SET id_usuario = ? WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profesor.getUsuario().getIdUsuario());
            pstmt.setInt(2, profesor.getIdProfesor());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating profesor failed, no rows affected.");
            }
            return profesor;
        }
    }
    
    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM Profesor WHERE id_profesor = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting profesor failed, no rows affected.");
            }
        }
    }
    
    @Override
    public Profesor findByCedula(String cedula) throws Exception {
        String sql = "SELECT p.*, u.* FROM Profesor p " +
                    "JOIN Usuario u ON p.id_usuario = u.id_usuario " +
                    "WHERE u.cedula = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cedula);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProfesor(rs);
            }
            
            return null;
        }
    }
    
    @Override
    public List<Profesor> findByEspecialidad(String especialidad) throws Exception {
        // Ya no existe el campo especialidad, así que devuelvo todos los profesores
        return readAll();
    }
    
    private Profesor mapResultSetToProfesor(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCedula(rs.getString("cedula"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setPassword(rs.getString("password"));
        usuario.setRol(rs.getString("rol"));
        Profesor profesor = new Profesor();
        profesor.setIdProfesor(rs.getInt("id_profesor"));
        profesor.setUsuario(usuario);
        return profesor;
    }
} 