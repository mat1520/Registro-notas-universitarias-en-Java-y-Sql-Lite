package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.universidad.dao.impl.ParcialDAOImpl;
import com.universidad.dao.impl.SubnotaDAOImpl;
import com.universidad.model.Parcial;
import com.universidad.model.Subnota;
import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class EstudianteController implements MainController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TableView<SubnotaRow> calificacionesTable;
    
    @FXML
    private TableColumn<SubnotaRow, String> materiaColumn;
    
    @FXML
    private TableColumn<SubnotaRow, String> profesorColumn;
    
    @FXML
    private TableColumn<SubnotaRow, String> parcialColumn;
    
    @FXML
    private TableColumn<SubnotaRow, Integer> subnotaNumColumn;
    
    @FXML
    private TableColumn<SubnotaRow, Double> notaColumn;
    
    @FXML
    private TableColumn<SubnotaRow, Double> totalParcialColumn;
    
    @FXML
    private TableColumn<SubnotaRow, Double> porcentajeFinalColumn;
    
    private Usuario usuario;
    
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        loadCalificaciones();
    }
    
    @FXML
    private void initialize() {
        materiaColumn.setCellValueFactory(new PropertyValueFactory<>("materia"));
        profesorColumn.setCellValueFactory(new PropertyValueFactory<>("profesor"));
        parcialColumn.setCellValueFactory(new PropertyValueFactory<>("parcial"));
        subnotaNumColumn.setCellValueFactory(new PropertyValueFactory<>("subnotaNum"));
        notaColumn.setCellValueFactory(new PropertyValueFactory<>("nota"));
        totalParcialColumn.setCellValueFactory(new PropertyValueFactory<>("totalParcial"));
        porcentajeFinalColumn.setCellValueFactory(new PropertyValueFactory<>("porcentajeFinal"));
    }
    
    private void loadCalificaciones() {
        String sql = "SELECT c.id_calificacion, m.nombre as materia, u.nombre || ' ' || u.apellido as profesor, cu.id_materia, cu.id_profesor " +
                "FROM Calificacion c " +
                "JOIN Inscripcion i ON c.id_inscripcion = i.id_inscripcion " +
                "JOIN Curso cu ON i.id_curso = cu.id_curso " +
                "JOIN Materia m ON cu.id_materia = m.id_materia " +
                "JOIN Profesor p ON cu.id_profesor = p.id_profesor " +
                "JOIN Usuario u ON p.id_usuario = u.id_usuario " +
                "JOIN Estudiante e ON i.id_estudiante = e.id_estudiante " +
                "WHERE e.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            ResultSet rs = pstmt.executeQuery();
            calificacionesTable.getItems().clear();
            SubnotaDAOImpl subnotaDAO = new SubnotaDAOImpl();
            ParcialDAOImpl parcialDAO = new ParcialDAOImpl();
            List<Parcial> parciales = parcialDAO.findAll();
            while (rs.next()) {
                int idCalificacion = rs.getInt("id_calificacion");
                String materia = rs.getString("materia");
                String profesor = rs.getString("profesor");
                List<Subnota> subnotas = subnotaDAO.findByCalificacion(idCalificacion);
                Map<Integer, List<Subnota>> subnotasPorParcial = new HashMap<>();
                for (Subnota s : subnotas) {
                    subnotasPorParcial.computeIfAbsent(s.getIdParcial(), k -> new ArrayList<>()).add(s);
                }
                double porcentajeFinal = 0;
                boolean todosCompletos = true;
                for (Parcial parcial : parciales) {
                    List<Subnota> subnotasParcial = subnotasPorParcial.getOrDefault(parcial.getIdParcial(), Collections.emptyList());
                    int esperado = parcial.getIdParcial() == 4 ? 4 : 3;
                    double totalParcial = 0;
                    if (subnotasParcial.size() == esperado) {
                        for (Subnota s : subnotasParcial) totalParcial += s.getValor();
                        porcentajeFinal += (totalParcial / (esperado * 10.0)) * parcial.getPorcentaje() * 100.0;
                    } else {
                        todosCompletos = false;
                    }
                    for (Subnota s : subnotasParcial) {
                        calificacionesTable.getItems().add(new SubnotaRow(
                            materia, profesor, parcial.getNombre(), s.getNumero(), s.getValor(),
                            subnotasParcial.size() == esperado ? totalParcial : null,
                            null
                        ));
                    }
                }
                // Agregar fila resumen de porcentaje final si todos los parciales están completos
                if (todosCompletos) {
                    calificacionesTable.getItems().add(new SubnotaRow(
                        materia, profesor, "TOTAL", null, null, null, porcentajeFinal
                    ));
                }
            }
        } catch (Exception e) {
            showError("Error al cargar calificaciones: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Gestión de Notas - Login");
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Error al cerrar sesión: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class SubnotaRow {
        private final String materia;
        private final String profesor;
        private final String parcial;
        private final Integer subnotaNum;
        private final Double nota;
        private final Double totalParcial;
        private final Double porcentajeFinal;
        
        public SubnotaRow(String materia, String profesor, String parcial, Integer subnotaNum, Double nota, Double totalParcial, Double porcentajeFinal) {
            this.materia = materia;
            this.profesor = profesor;
            this.parcial = parcial;
            this.subnotaNum = subnotaNum;
            this.nota = nota;
            this.totalParcial = totalParcial;
            this.porcentajeFinal = porcentajeFinal;
        }
        
        public String getMateria() { return materia; }
        public String getProfesor() { return profesor; }
        public String getParcial() { return parcial; }
        public Integer getSubnotaNum() { return subnotaNum; }
        public Double getNota() { return nota; }
        public Double getTotalParcial() { return totalParcial; }
        public Double getPorcentajeFinal() { return porcentajeFinal; }
    }
} 