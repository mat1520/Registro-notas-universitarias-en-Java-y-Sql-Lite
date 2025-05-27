package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    
    @FXML
    private TextField cedulaField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private void handleLogin() {
        String cedula = cedulaField.getText();
        String password = passwordField.getText();
        
        if (cedula.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor complete todos los campos");
            return;
        }
        if (!validarCedulaEcuatoriana(cedula)) {
            errorLabel.setText("Cédula ecuatoriana no válida");
            return;
        }
        
        try {
            Usuario usuario = validateUser(cedula, password);
            if (usuario != null) {
                openMainWindow(usuario);
            } else {
                errorLabel.setText("Cédula o contraseña incorrectos");
            }
        } catch (Exception e) {
            errorLabel.setText("Error al iniciar sesión: " + e.getMessage());
        }
    }
    
    private Usuario validateUser(String cedula, String password) throws Exception {
        String sql = "SELECT * FROM Usuario WHERE cedula = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cedula);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setCedula(rs.getString("cedula"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellido(rs.getString("apellido"));
                usuario.setPassword(rs.getString("password"));
                usuario.setRol(rs.getString("rol"));
                return usuario;
            }
            
            return null;
        }
    }
    
    private void openMainWindow(Usuario usuario) throws Exception {
        String fxmlFile;
        String title;
        
        switch (usuario.getRol()) {
            case "ESTUDIANTE":
                fxmlFile = "/fxml/EstudianteView.fxml";
                title = "Vista de Estudiante";
                break;
            case "PROFESOR":
                fxmlFile = "/fxml/ProfesorView.fxml";
                title = "Vista de Profesor";
                break;
            case "ADMIN":
                fxmlFile = "/fxml/AdminView.fxml";
                title = "Vista de Administrador";
                break;
            default:
                throw new Exception("Rol no válido");
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            // Pasar el usuario al controlador de la vista principal
            Object controller = loader.getController();
            if (controller instanceof MainController) {
                ((MainController) controller).setUsuario(usuario);
            }
            Stage stage = (Stage) cedulaField.getScene().getWindow();
            stage.setTitle(title);
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
        } catch (Exception e) {
            e.printStackTrace(); // Mostrar el error real en consola
            throw new Exception("Error al cargar la vista: " + e.getMessage());
        }
    }

    // Validación de cédula ecuatoriana
    public static boolean validarCedulaEcuatoriana(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) return false;
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
        if (provincia < 1 || provincia > 24) return false;
        if (tercerDigito < 0 || tercerDigito > 6) return false;
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int valor = Character.getNumericValue(cedula.charAt(i));
            if (i % 2 == 0) { // posición impar
                valor *= 2;
                if (valor > 9) valor -= 9;
            }
            suma += valor;
        }
        int digitoVerificador = (10 - (suma % 10)) % 10;
        return digitoVerificador == Character.getNumericValue(cedula.charAt(9));
    }
} 