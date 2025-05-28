package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class EstudianteController implements MainController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private ComboBox<MateriaItem> materiaComboBox;
    @FXML
    private TableView<SubnotaRow> subnotasTable;
    @FXML
    private TableColumn<SubnotaRow, Integer> parcialColumn;
    @FXML
    private TableColumn<SubnotaRow, Integer> numeroColumn;
    @FXML
    private TableColumn<SubnotaRow, Double> valorColumn;
    @FXML
    private Label totalLabel;

    private Usuario usuario;

    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        loadMaterias();
    }

    @FXML
    private void initialize() {
        parcialColumn.setCellValueFactory(new PropertyValueFactory<>("parcial"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        valorColumn.setCellValueFactory(new PropertyValueFactory<>("valor"));
        materiaComboBox.setOnAction(e -> loadSubnotas());
    }

    private void loadMaterias() {
        ObservableList<MateriaItem> materias = FXCollections.observableArrayList();
        String sql = "SELECT m.id_materia, m.nombre FROM Materia m " +
                "JOIN Estudiante e ON m.id_carrera = e.id_carrera " +
                "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                "WHERE u.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                materias.add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
            }
            materiaComboBox.setItems(materias);
            if (!materias.isEmpty()) materiaComboBox.setValue(materias.get(0));
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
        }
    }

    private void loadSubnotas() {
        MateriaItem materia = materiaComboBox.getValue();
        if (materia == null) {
            subnotasTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("");
            return;
        }
        ObservableList<SubnotaRow> rows = FXCollections.observableArrayList();
        double suma = 0.0;
        String sql = "SELECT c.id_calificacion FROM Calificacion c " +
                "JOIN Curso cu ON c.id_curso = cu.id_curso " +
                "WHERE c.id_estudiante = (SELECT e.id_estudiante FROM Estudiante e WHERE e.id_usuario = ?) AND cu.id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            pstmt.setInt(2, materia.id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int idCalificacion = rs.getInt("id_calificacion");
                com.universidad.dao.SubnotaDAO subnotaDAO = new com.universidad.dao.impl.SubnotaDAOImpl();
                for (com.universidad.model.Subnota s : subnotaDAO.findByCalificacion(idCalificacion)) {
                    rows.add(new SubnotaRow(s.getParcial(), s.getNumero(), s.getValor()));
                    suma += s.getValor();
                }
            }
            subnotasTable.setItems(rows);
            totalLabel.setText("Total: " + suma + " / 100 (" + (int)(suma) + "%)");
        } catch (Exception e) {
            showError("Error al cargar subnotas: " + e.getMessage());
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

    private static class MateriaItem {
        int id;
        String nombre;
        MateriaItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
    public static class SubnotaRow {
        private final int parcial;
        private final int numero;
        private final double valor;
        public SubnotaRow(int parcial, int numero, double valor) {
            this.parcial = parcial;
            this.numero = numero;
            this.valor = valor;
        }
        public int getParcial() { return parcial; }
        public int getNumero() { return numero; }
        public double getValor() { return valor; }
    }
} 