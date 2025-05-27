package com.universidad.controller;

import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.universidad.dao.impl.ParcialDAOImpl;
import com.universidad.dao.impl.SubnotaDAOImpl;
import com.universidad.model.Parcial;
import com.universidad.model.Subnota;

public class ProfesorController implements MainController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ComboBox<CursoItem> cursoComboBox;
    
    @FXML
    private TableView<EstudianteNotaRow> estudiantesTable;
    
    @FXML
    private TableColumn<EstudianteNotaRow, String> matriculaColumn;
    
    @FXML
    private TableColumn<EstudianteNotaRow, String> nombreColumn;
    
    @FXML
    private TableColumn<EstudianteNotaRow, Double> notaColumn;
    
    @FXML
    private TableColumn<EstudianteNotaRow, Void> accionesColumn;
    
    private Usuario usuario;
    
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        loadCursos();
    }
    
    @FXML
    private void initialize() {
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        notaColumn.setCellValueFactory(new PropertyValueFactory<>("nota"));
        
        // Configurar columna de acciones
        accionesColumn.setCellFactory(createButtonCellFactory());
    }
    
    private void loadCursos() {
        String sql = "SELECT c.id_curso, m.nombre as materia, c.periodo, c.seccion " +
                    "FROM Curso c " +
                    "JOIN Materia m ON c.id_materia = m.id_materia " +
                    "JOIN Profesor p ON c.id_profesor = p.id_profesor " +
                    "WHERE p.id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, usuario.getIdUsuario());
            ResultSet rs = pstmt.executeQuery();
            
            cursoComboBox.getItems().clear();
            while (rs.next()) {
                CursoItem curso = new CursoItem(
                    rs.getInt("id_curso"),
                    rs.getString("materia"),
                    rs.getString("periodo"),
                    rs.getString("seccion")
                );
                cursoComboBox.getItems().add(curso);
            }
        } catch (Exception e) {
            showError("Error al cargar cursos: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLoadEstudiantes() {
        CursoItem selectedCurso = cursoComboBox.getValue();
        if (selectedCurso == null) {
            showError("Por favor seleccione un curso");
            return;
        }
        
        String sql = "SELECT e.matricula, u.nombre || ' ' || u.apellido as nombre, " +
                    "c.nota, c.fecha_calificacion, c.observaciones, i.id_inscripcion " +
                    "FROM Inscripcion i " +
                    "JOIN Estudiante e ON i.id_estudiante = e.id_estudiante " +
                    "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                    "LEFT JOIN Calificacion c ON c.id_inscripcion = i.id_inscripcion " +
                    "WHERE i.id_curso = ? AND i.estado = 'ACTIVA'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, selectedCurso.getIdCurso());
            ResultSet rs = pstmt.executeQuery();
            
            estudiantesTable.getItems().clear();
            while (rs.next()) {
                LocalDate fecha = null;
                try {
                    java.sql.Date sqlDate = rs.getDate("fecha_calificacion");
                    if (sqlDate != null) {
                        fecha = sqlDate.toLocalDate();
                    }
                } catch (Exception ex) {
                    fecha = null;
                }
                EstudianteNotaRow row = new EstudianteNotaRow(
                    rs.getString("matricula"),
                    rs.getString("nombre"),
                    rs.getDouble("nota"),
                    fecha,
                    rs.getString("observaciones"),
                    rs.getInt("id_inscripcion")
                );
                estudiantesTable.getItems().add(row);
            }
        } catch (Exception e) {
            showError("Error al cargar estudiantes: " + e.getMessage());
        }
    }
    
    private Callback<TableColumn<EstudianteNotaRow, Void>, TableCell<EstudianteNotaRow, Void>> createButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<EstudianteNotaRow, Void> call(TableColumn<EstudianteNotaRow, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Asignar Nota");
                    
                    {
                        button.setOnAction(event -> {
                            EstudianteNotaRow row = getTableView().getItems().get(getIndex());
                            showNotaDialog(row);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : button);
                    }
                };
            }
        };
    }
    
    private void showNotaDialog(EstudianteNotaRow row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Asignar Subnota");
        dialog.setHeaderText("Asignar subnota para " + row.getNombre());

        // Solo 3 parciales
        List<Parcial> parciales = new ArrayList<>();
        parciales.add(new Parcial(1, "Parcial 1", 0.3));
        parciales.add(new Parcial(2, "Parcial 2", 0.3));
        parciales.add(new Parcial(3, "Parcial 3", 0.4));

        ComboBox<Parcial> parcialCombo = new ComboBox<>();
        parcialCombo.getItems().addAll(parciales);
        parcialCombo.setValue(parciales.get(0));

        ComboBox<Integer> subnotaNumCombo = new ComboBox<>();
        parcialCombo.setOnAction(ev -> {
            int max = parcialCombo.getValue().getIdParcial() == 3 ? 4 : 3;
            subnotaNumCombo.getItems().clear();
            for (int i = 1; i <= max; i++) subnotaNumCombo.getItems().add(i);
            subnotaNumCombo.setValue(1);
        });
        parcialCombo.getOnAction().handle(null);

        TextField valorField = new TextField();
        valorField.setPromptText("0-10");

        TableView<SubnotaResumenRow> resumenTable = new TableView<>();
        TableColumn<SubnotaResumenRow, String> parcialCol = new TableColumn<>("Parcial");
        parcialCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getParcial()));
        TableColumn<SubnotaResumenRow, Integer> numCol = new TableColumn<>("Subnota");
        numCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getNumero()).asObject());
        TableColumn<SubnotaResumenRow, Double> valorCol = new TableColumn<>("Valor");
        valorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValor()).asObject());
        resumenTable.getColumns().addAll(parcialCol, numCol, valorCol);
        resumenTable.setPrefHeight(120);

        // Cargar resumen
        try {
            int idCalificacion = obtenerIdCalificacionPorInscripcion(row.getIdInscripcion());
            SubnotaDAOImpl subnotaDAO = new SubnotaDAOImpl();
            List<Subnota> subnotas = subnotaDAO.findByCalificacion(idCalificacion);
            Map<Integer, Parcial> parcialesMap = new HashMap<>();
            for (Parcial p : parciales) parcialesMap.put(p.getIdParcial(), p);
            for (Subnota s : subnotas) {
                Parcial p = parcialesMap.get(s.getIdParcial());
                if (p != null) {
                    resumenTable.getItems().add(new SubnotaResumenRow(
                        p.getNombre(),
                        s.getNumero(),
                        s.getValor()
                    ));
                }
            }
        } catch (Exception e) {
            // ignorar
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Parcial:"), 0, 0); grid.add(parcialCombo, 1, 0);
        grid.add(new Label("Subnota:"), 0, 1); grid.add(subnotaNumCombo, 1, 1);
        grid.add(new Label("Valor (0-10):"), 0, 2); grid.add(valorField, 1, 2);
        grid.add(new Label("Subnotas actuales:"), 0, 3, 2, 1);
        grid.add(resumenTable, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double valor = Double.parseDouble(valorField.getText());
                    if (valor < 0 || valor > 10) {
                        showError("La subnota debe estar entre 0 y 10");
                        return null;
                    }
                    int idCalificacion = obtenerIdCalificacionPorInscripcion(row.getIdInscripcion());
                    SubnotaDAOImpl subnotaDAO = new SubnotaDAOImpl();
                    Subnota subnota = new Subnota(null, idCalificacion, parcialCombo.getValue().getIdParcial(), subnotaNumCombo.getValue(), valor);
                    subnotaDAO.save(subnota);
                    showInfo("Subnota guardada correctamente.");
                    handleLoadEstudiantes();
                } catch (Exception e) {
                    showError("Error al guardar subnota: " + e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // Utilidad para obtener id_calificacion dado id_inscripcion
    private int obtenerIdCalificacionPorInscripcion(int idInscripcion) throws Exception {
        String sql = "SELECT id_calificacion FROM Calificacion WHERE id_inscripcion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idInscripcion);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new Exception("No existe calificación para la inscripción");
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clase para la tabla resumen
    private static class SubnotaResumenRow {
        private final String parcial;
        private final int numero;
        private final double valor;
        public SubnotaResumenRow(String parcial, int numero, double valor) {
            this.parcial = parcial;
            this.numero = numero;
            this.valor = valor;
        }
        public String getParcial() { return parcial; }
        public int getNumero() { return numero; }
        public double getValor() { return valor; }
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
    
    public static class CursoItem {
        private final int idCurso;
        private final String materia;
        private final String periodo;
        private final String seccion;
        
        public CursoItem(int idCurso, String materia, String periodo, String seccion) {
            this.idCurso = idCurso;
            this.materia = materia;
            this.periodo = periodo;
            this.seccion = seccion;
        }
        
        public int getIdCurso() { return idCurso; }
        public String getMateria() { return materia; }
        public String getPeriodo() { return periodo; }
        public String getSeccion() { return seccion; }
        
        @Override
        public String toString() {
            return materia + " - " + periodo + " - Sección " + seccion;
        }
    }
    
    public static class EstudianteNotaRow {
        private final String matricula;
        private final String nombre;
        private double nota;
        private LocalDate fecha;
        private String observaciones;
        private final int idInscripcion;
        
        public EstudianteNotaRow(String matricula, String nombre, double nota, LocalDate fecha, 
                               String observaciones, int idInscripcion) {
            this.matricula = matricula;
            this.nombre = nombre;
            this.nota = nota;
            this.fecha = fecha;
            this.observaciones = observaciones;
            this.idInscripcion = idInscripcion;
        }
        
        public String getMatricula() { return matricula; }
        public String getNombre() { return nombre; }
        public double getNota() { return nota; }
        public LocalDate getFecha() { return fecha; }
        public String getObservaciones() { return observaciones; }
        public int getIdInscripcion() { return idInscripcion; }
        
        public void setNota(double nota) { this.nota = nota; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
} 