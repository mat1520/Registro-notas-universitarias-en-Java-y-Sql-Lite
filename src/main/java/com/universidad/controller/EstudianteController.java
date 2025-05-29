package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre_usuario() + " " + usuario.getApellido_usuario());
        loadMaterias();
    }

    @FXML
    private void initialize() {
        parcialColumn.setCellValueFactory(new PropertyValueFactory<>("parcial_nombre"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero_subnota"));
        valorColumn.setCellValueFactory(new PropertyValueFactory<>("valor"));
        materiaComboBox.setOnAction(e -> loadSubnotas());
    }

    private void loadMaterias() {
        ObservableList<MateriaItem> materias = FXCollections.observableArrayList();
        String sql = "SELECT m.id_materia, m.nombre_materia FROM Materia m " +
                "JOIN Estudiante e ON m.id_carrera = e.id_carrera " +
                "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                "WHERE u.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                materias.add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
            }
            materiaComboBox.setItems(materias);
            if (!materias.isEmpty()) {
                materiaComboBox.setValue(materias.get(0));
                loadSubnotas();
            }
        } catch (SQLException e) {
            showError("Error al cargar materias: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar materias: " + e.getMessage());
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
        String sql = "SELECT sn.id_subnota, p.Nombre as parcial_nombre, sn.Numero as numero_subnota, sn.valor, sn.id_calificacion FROM Subnota sn " +
                     "JOIN Calificacion c ON sn.id_calificacion = c.id_calificacion " +
                     "JOIN Curso cu ON c.id_curso = cu.id_curso " +
                     "JOIN Parcial p ON sn.id_parcial = p.id_parcial " +
                     "WHERE c.id_estudiante = (SELECT e.id_estudiante FROM Estudiante e WHERE e.id_usuario = ?) AND cu.id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            pstmt.setInt(2, materia.id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rows.add(new SubnotaRow(rs.getString("parcial_nombre"), rs.getInt("numero_subnota"), rs.getDouble("valor")));
                suma += rs.getDouble("valor");
            }
            subnotasTable.setItems(rows);
            totalLabel.setText("Total: " + suma + " / 100 (" + (int)(suma) + "%)");
        } catch (SQLException e) {
            showError("Error al cargar subnotas: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar subnotas: " + e.getMessage());
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
            stage.hide();
            stage.setMaximized(true);
            stage.show();
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
        private String nombre_materia;
        MateriaItem(int id, String nombre_materia) {
            this.id = id;
            this.nombre_materia = nombre_materia;
        }
        public int getId() { return id; }
        @Override public String toString() { return nombre_materia; }
    }
    public static class SubnotaRow {
        private final String parcial_nombre;
        private final int numero_subnota;
        private final double valor;
        public SubnotaRow(String parcial_nombre, int numero_subnota, double valor) {
            this.parcial_nombre = parcial_nombre;
            this.numero_subnota = numero_subnota;
            this.valor = valor;
        }
        public String getParcial_nombre() { return parcial_nombre; }
        public int getNumero_subnota() { return numero_subnota; }
        public double getValor() { return valor; }
    }
} 