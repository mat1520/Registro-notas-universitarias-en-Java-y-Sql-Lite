package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ProfesorController implements MainController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private ComboBox<CursoItem> cursoComboBox;
    @FXML
    private TableView<SubnotaRow> subnotasTable;
    @FXML
    private TableColumn<SubnotaRow, String> estudianteColumn;
    @FXML
    private TableColumn<SubnotaRow, Integer> parcialColumn;
    @FXML
    private TableColumn<SubnotaRow, Integer> numeroColumn;
    @FXML
    private TableColumn<SubnotaRow, Double> valorColumn;
    @FXML
    private TableColumn<SubnotaRow, Void> accionesColumn;

    private Usuario usuario;

    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre_usuario() + " " + usuario.getApellido_usuario());
        loadCursos();
    }

    @FXML
    private void initialize() {
        setupSubnotasTable();
        cursoComboBox.setOnAction(e -> {
            loadSubnotas();
        });
    }

    private void loadCursos() {
        String sql = "SELECT c.id_curso, m.nombre_materia as materia FROM Curso c " +
                     "JOIN Materia m ON c.id_materia = m.id_materia " +
                     "JOIN Profesor p ON c.id_profesor = p.id_profesor " +
                     "JOIN Usuario u ON p.id_usuario = u.id_usuario " +
                     "WHERE u.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuario.getIdUsuario());
            ResultSet rs = pstmt.executeQuery();
            cursoComboBox.getItems().clear();
            while (rs.next()) {
                cursoComboBox.getItems().add(new CursoItem(
                    rs.getInt("id_curso"),
                    rs.getString("materia")
                ));
            }
        } catch (Exception e) {
            showError("Error al cargar cursos: " + e.getMessage());
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

    @FXML
    private void handleAgregarSubnota() {
        CursoItem selectedCurso = cursoComboBox.getValue();
        if (selectedCurso == null) {
            showError("Por favor seleccione un curso");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Obtener id_materia y carrera del curso
            int idMateria = -1;
            int idCarrera = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT m.id_materia, m.id_carrera FROM Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE c.id_curso = ?")) {
                pstmt.setInt(1, selectedCurso.getIdCurso());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    idMateria = rs.getInt("id_materia");
                    idCarrera = rs.getInt("id_carrera");
                }
            }

            if (idCarrera == -1) {
                showError("No se pudo determinar la carrera del curso");
                return;
            }

            List<EstudianteItem> estudiantes = new ArrayList<>();
            String sqlEst = "SELECT e.id_estudiante, u.nombre_usuario || ' ' || u.apellido_usuario as nombre FROM Estudiante e JOIN Usuario u ON e.id_usuario = u.id_usuario WHERE e.id_carrera = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlEst)) {
                pstmt.setInt(1, idCarrera);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    estudiantes.add(new EstudianteItem(rs.getInt("id_estudiante"), rs.getString("nombre")));
                }
            }

            if (estudiantes.isEmpty()) {
                showError("No hay estudiantes registrados en esta carrera");
                return;
            }

            Dialog<SubnotaInput> dialog = new Dialog<>();
            dialog.setTitle("Agregar Subnota");
            dialog.setHeaderText("Complete los datos de la subnota");
            
            ComboBox<EstudianteItem> estudianteCombo = new ComboBox<>();
            estudianteCombo.getItems().addAll(estudiantes);
            estudianteCombo.setValue(estudiantes.get(0));
            
            ComboBox<Integer> parcialCombo = new ComboBox<>();
            parcialCombo.getItems().addAll(1, 2, 3);
            parcialCombo.setValue(1);
            
            // Spinner para el número de subnota
            Spinner<Integer> numeroSpinner = new Spinner<>(1, 3, 1);
            numeroSpinner.setEditable(false);
            
            // TextField mejorado para el valor de la nota
            TextField valorTextField = new TextField();
            valorTextField.setPromptText("0.00 - 10.00");
            valorTextField.setPrefWidth(100);
            
            // Validación mejorada para el valor de la nota
            valorTextField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) return;
                
                // Permitir solo números, un punto decimal y hasta 2 decimales
                if (!newValue.matches("\\d*\\.?\\d{0,2}")) {
                    valorTextField.setText(oldValue);
                    return;
                }
                
                try {
                    if (newValue.equals(".")) {
                        // Permitir escribir el punto decimal
                        return;
                    }
                    
                    double value = Double.parseDouble(newValue);
                    if (value > 10.0) {
                        valorTextField.setText("10.00");
                    }
                } catch (NumberFormatException e) {
                    valorTextField.setText(oldValue);
                }
            });

            // Formatear al perder el foco
            valorTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused && !valorTextField.getText().isEmpty()) {
                    try {
                        double value = Double.parseDouble(valorTextField.getText());
                        valorTextField.setText(String.format("%.2f", value));
                    } catch (NumberFormatException e) {
                        valorTextField.setText("0.00");
                    }
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Estudiante:"), 0, 0);
            grid.add(estudianteCombo, 1, 0);
            grid.add(new Label("Parcial:"), 0, 1);
            grid.add(parcialCombo, 1, 1);
            grid.add(new Label("N° Subnota:"), 0, 2);
            grid.add(numeroSpinner, 1, 2);
            grid.add(new Label("Valor (0-10):"), 0, 3);
            grid.add(valorTextField, 1, 3);
            
            // Actualizar rango de número de subnota según el parcial seleccionado
            parcialCombo.setOnAction(e -> {
                int parcial = parcialCombo.getValue();
                if (parcial == 1) {
                    numeroSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1));
                } else if (parcial == 2) {
                    numeroSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 6, 4));
                } else {
                    numeroSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 10, 7));
                }
            });

            dialog.getDialogPane().setContent(grid);
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String valorText = valorTextField.getText();
                    double valor;
                    try {
                        valor = Double.parseDouble(valorText);
                    } catch (NumberFormatException ex) {
                        showError("El valor de la subnota debe ser un número válido");
                        return null;
                    }
                    
                    int parcial = parcialCombo.getValue();
                    int numero = numeroSpinner.getValue();
                    
                    // Validar rango de subnota según parcial
                    if (!validarRangoSubnota(parcial, numero)) {
                        return null;
                    }
                    
                    // Validar valor de la subnota
                    if (!validarValorSubnota(valor)) {
                        return null;
                    }
                    
                    return new SubnotaInput(estudianteCombo.getValue(), parcial, numero, valor);
                }
                return null;
            });

            Optional<SubnotaInput> result = dialog.showAndWait();
            if (result.isPresent()) {
                SubnotaInput input = result.get();
                
                // Obtener o crear calificación
                int idCalificacion = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT id_calificacion FROM Calificacion WHERE id_estudiante = ? AND id_curso = ?")) {
                    pstmt.setInt(1, input.estudiante.getId());
                    pstmt.setInt(2, selectedCurso.getIdCurso());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        idCalificacion = rs.getInt("id_calificacion");
                    }
                }

                if (idCalificacion == -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO Calificacion (id_estudiante, id_curso, estado) VALUES (?, ?, 'NO_CALIFICADO')", 
                            PreparedStatement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, input.estudiante.getId());
                        pstmt.setInt(2, selectedCurso.getIdCurso());
                        pstmt.executeUpdate();
                        
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                idCalificacion = generatedKeys.getInt(1);
                            } else {
                                showError("Error al crear la calificación");
                                return;
                            }
                        }
                    }
                }

                // Obtener id_parcial
                String nombreParcial = "Primer Parcial";
                if (input.numeroParcialSeleccionado == 2) {
                    nombreParcial = "Segundo Parcial";
                } else if (input.numeroParcialSeleccionado == 3) {
                    nombreParcial = "Examen Final";
                }
                
                int idParcial = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT id_parcial FROM Parcial WHERE nombre = ?")) {
                    pstmt.setString(1, nombreParcial);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        idParcial = rs.getInt("id_parcial");
                    }
                }

                if (idParcial == -1) {
                    showError("No se encontró el parcial seleccionado");
                    return;
                }

                // Validar que no exista ya una subnota con ese número en ese parcial
                if (!validarSubnotaExistente(conn, idCalificacion, idParcial, input.numero, null)) {
                    return;
                }

                // Crear subnota
                insertSubnota(conn, idCalificacion, idParcial, input.numero, input.valor);
                showInfo("Subnota agregada correctamente");
                loadSubnotas();
            }
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
        }
    }

    private boolean validarRangoSubnota(int parcial, int numero) {
        if (parcial == 1 && (numero < 1 || numero > 3)) {
            showError("El Primer Parcial solo acepta subnotas del 1 al 3");
            return false;
        } else if (parcial == 2 && (numero < 4 || numero > 6)) {
            showError("El Segundo Parcial solo acepta subnotas del 4 al 6");
            return false;
        } else if (parcial == 3 && (numero < 7 || numero > 10)) {
            showError("El Examen Final solo acepta subnotas del 7 al 10");
            return false;
        }
        return true;
    }

    private boolean validarValorSubnota(double valor) {
        if (valor < 0 || valor > 10) {
            showError("El valor de la subnota debe estar entre 0 y 10");
            return false;
        }
        // Validar que el valor tenga máximo 2 decimales
        String valorStr = String.format("%.2f", valor);
        try {
            double valorRedondeado = Double.parseDouble(valorStr);
            if (valorRedondeado != valor) {
                showError("El valor de la subnota solo puede tener hasta 2 decimales");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Error al validar el formato del valor");
            return false;
        }
        return true;
    }

    private boolean validarSubnotaExistente(Connection conn, int idCalificacion, int idParcial, int numero, Integer idSubnotaExcluir) throws SQLException {
        List<SubnotaRow> subnotas = findSubnotasByCalificacion(conn, idCalificacion);
        
        String nombreParcial = "Primer Parcial";
        if (idParcial == 2) nombreParcial = "Segundo Parcial";
        else if (idParcial == 3) nombreParcial = "Examen Final";
        
        for (SubnotaRow s : subnotas) {
            if (s.getId_parcial_db() == idParcial && 
                s.getNumero_nota() == numero && 
                (idSubnotaExcluir == null || s.getIdSubnota() != idSubnotaExcluir)) {
                showError(String.format("Ya existe una subnota número %d para el %s. Por favor, elija otro número de subnota.", 
                          numero, nombreParcial));
                return false;
            }
        }
        return true;
    }

    private static class EstudianteItem {
        private final int id;
        private final String nombre_completo;

        EstudianteItem(int id, String nombre_completo) {
            this.id = id;
            this.nombre_completo = nombre_completo;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return nombre_completo; }
    }

    private static class SubnotaInput {
        private final EstudianteItem estudiante;
        private final int numeroParcialSeleccionado;
        private final int numero;
        private final double valor;

        SubnotaInput(EstudianteItem estudiante, int numeroParcialSeleccionado, int numero, double valor) {
            this.estudiante = estudiante;
            this.numeroParcialSeleccionado = numeroParcialSeleccionado;
            this.numero = numero;
            this.valor = valor;
        }
    }

    public static class SubnotaRow {
        private final int idSubnota;
        private final int idEstudiante;
        private final String nombre_completo;
        private final int id_parcial_db;
        private final int numero_nota;
        private final double valor;
        private final int idCalificacion;
        private final String parcial_nombre;

        public SubnotaRow(int idSubnota, int idEstudiante, String nombre_completo, int id_parcial_db, 
                         int numero_nota, double valor, int idCalificacion, String parcial_nombre) {
            this.idSubnota = idSubnota;
            this.idEstudiante = idEstudiante;
            this.nombre_completo = nombre_completo;
            this.id_parcial_db = id_parcial_db;
            this.numero_nota = numero_nota;
            this.valor = valor;
            this.idCalificacion = idCalificacion;
            this.parcial_nombre = parcial_nombre;
        }

        public int getIdSubnota() { return idSubnota; }
        public int getIdEstudiante() { return idEstudiante; }
        public String getNombre_completo() { return nombre_completo; }
        public int getId_parcial_db() { return id_parcial_db; }
        public int getNumero_nota() { return numero_nota; }
        public double getValor() { return valor; }
        public int getIdCalificacion() { return idCalificacion; }
        public String getParcial_nombre() { return parcial_nombre; }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        private final String nombre;
        public CursoItem(int idCurso, String nombre) {
            this.idCurso = idCurso;
            this.nombre = nombre;
        }
        public int getIdCurso() { return idCurso; }
        @Override public String toString() { return nombre; }
    }

    private void loadSubnotas() {
        CursoItem selectedCurso = cursoComboBox.getValue();
        if (selectedCurso == null) {
            subnotasTable.setItems(FXCollections.observableArrayList());
            return;
        }
        ObservableList<SubnotaRow> rows = FXCollections.observableArrayList();
        String sql = "SELECT sn.id_subnota, sn.id_calificacion, sn.id_parcial, sn.numero_nota, sn.valor, " +
                     "p.nombre as parcial_nombre, " +
                     "c.id_estudiante, " +
                     "u.nombre_usuario || ' ' || u.apellido_usuario as nombre_completo " +
                     "FROM Subnota sn " +
                     "JOIN Calificacion c ON sn.id_calificacion = c.id_calificacion " +
                     "JOIN Estudiante e ON c.id_estudiante = e.id_estudiante " +
                     "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                     "JOIN Curso cu ON c.id_curso = cu.id_curso " +
                     "JOIN Parcial p ON sn.id_parcial = p.id_parcial " +
                     "WHERE cu.id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedCurso.getIdCurso());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rows.add(new SubnotaRow(
                    rs.getInt("id_subnota"),
                    rs.getInt("id_estudiante"),
                    rs.getString("nombre_completo"),
                    rs.getInt("id_parcial"),
                    rs.getInt("numero_nota"),
                    rs.getDouble("valor"),
                    rs.getInt("id_calificacion"),
                    rs.getString("parcial_nombre")
                ));
            }
            subnotasTable.setItems(rows);
        } catch (Exception e) {
            showError("Error al cargar subnotas: " + e.getMessage());
        }
    }

    private void setupSubnotasTable() {
        estudianteColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_completo"));
        parcialColumn.setCellValueFactory(new PropertyValueFactory<>("parcial_nombre"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero_nota"));
        valorColumn.setCellValueFactory(new PropertyValueFactory<>("valor"));
        
        // Formatear la columna de valor para mostrar solo 2 decimales
        valorColumn.setCellFactory(tc -> new TableCell<SubnotaRow, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", valor));
                }
            }
        });

        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");
            private final HBox buttons = new HBox(5, editButton, deleteButton);
            {
                editButton.setOnAction(event -> {
                    SubnotaRow row = getTableView().getItems().get(getIndex());
                    showEditSubnotaDialog(row);
                });
                deleteButton.setOnAction(event -> {
                    SubnotaRow row = getTableView().getItems().get(getIndex());
                    handleDeleteSubnota(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void showEditSubnotaDialog(SubnotaRow row) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Dialog<Double> dialog = new Dialog<>();
            dialog.setTitle("Editar Subnota");
            dialog.setHeaderText("Editar valor de la subnota");

            // TextField mejorado para el valor de la nota
            TextField valorTextField = new TextField(String.format("%.2f", row.getValor()));
            valorTextField.setPromptText("0.00 - 10.00");
            valorTextField.setPrefWidth(100);
            
            // Validación mejorada para el valor de la nota
            valorTextField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) return;
                
                // Permitir solo números, un punto decimal y hasta 2 decimales
                if (!newValue.matches("\\d*\\.?\\d{0,2}")) {
                    valorTextField.setText(oldValue);
                    return;
                }
                
                try {
                    if (newValue.equals(".")) {
                        // Permitir escribir el punto decimal
                        return;
                    }
                    
                    double value = Double.parseDouble(newValue);
                    if (value > 10.0) {
                        valorTextField.setText("10.00");
                    }
                } catch (NumberFormatException e) {
                    valorTextField.setText(oldValue);
                }
            });

            // Formatear al perder el foco
            valorTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused && !valorTextField.getText().isEmpty()) {
                    try {
                        double value = Double.parseDouble(valorTextField.getText());
                        valorTextField.setText(String.format("%.2f", value));
                    } catch (NumberFormatException e) {
                        valorTextField.setText("0.00");
                    }
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Valor (0-10):"), 0, 0);
            grid.add(valorTextField, 1, 0);
            dialog.getDialogPane().setContent(grid);
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String valorText = valorTextField.getText();
                    double valor;
                    try {
                        valor = Double.parseDouble(valorText);
                    } catch (NumberFormatException ex) {
                        showError("El valor de la subnota debe ser un número válido");
                        return null;
                    }
                    
                    // Validar valor de la subnota
                    if (!validarValorSubnota(valor)) {
                        return null;
                    }
                    
                    return valor;
                }
                return null;
            });

            Optional<Double> result = dialog.showAndWait();
            if (result.isPresent()) {
                double valorNuevo = result.get();
                
                // Actualizar subnota
                updateSubnota(conn, row.getIdSubnota(), valorNuevo);
                showInfo("Subnota actualizada correctamente");
                loadSubnotas();
            }
        } catch (SQLException e) {
            showError("Error al actualizar la subnota: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
        }
    }

    private void handleDeleteSubnota(SubnotaRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta subnota?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                deleteSubnota(conn, row.getIdSubnota());
                showInfo("Subnota eliminada correctamente.");
                loadSubnotas();
            } catch (SQLException e) {
                showError("Error al eliminar subnota: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleActualizarSubnotas() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadSubnotas();
        } catch (SQLException e) {
            showError("Error al actualizar subnotas: " + e.getMessage());
        }
    }

    private List<SubnotaRow> findSubnotasByCalificacion(Connection conn, int idCalificacion) throws SQLException {
        List<SubnotaRow> subnotas = new ArrayList<>();
        String sql = "SELECT s.id_subnota, s.id_calificacion, s.id_parcial, s.numero_nota, s.valor, " +
                    "p.nombre as parcial_nombre, " +
                    "c.id_estudiante, " +
                    "u.nombre_usuario || ' ' || u.apellido_usuario as nombre_completo " +
                    "FROM Subnota s " +
                    "JOIN Parcial p ON s.id_parcial = p.id_parcial " +
                    "JOIN Calificacion c ON s.id_calificacion = c.id_calificacion " +
                    "JOIN Estudiante e ON c.id_estudiante = e.id_estudiante " +
                    "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                    "WHERE s.id_calificacion = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCalificacion);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subnotas.add(new SubnotaRow(
                    rs.getInt("id_subnota"),
                    rs.getInt("id_estudiante"),
                    rs.getString("nombre_completo"),
                    rs.getInt("id_parcial"),
                    rs.getInt("numero_nota"),
                    rs.getDouble("valor"),
                    rs.getInt("id_calificacion"),
                    rs.getString("parcial_nombre")
                ));
            }
        }
        return subnotas;
    }

    private void insertSubnota(Connection conn, int idCalificacion, int idParcial, int numero, double valor) throws SQLException {
        String sql = "INSERT INTO Subnota (id_calificacion, id_parcial, numero_nota, valor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCalificacion);
            pstmt.setInt(2, idParcial);
            pstmt.setInt(3, numero);
            pstmt.setDouble(4, valor);
            pstmt.executeUpdate();
        }
    }

    private void updateSubnota(Connection conn, int idSubnota, double valor) throws SQLException {
        String sql = "UPDATE Subnota SET valor = ? WHERE id_subnota = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, valor);
            pstmt.setInt(2, idSubnota);
            pstmt.executeUpdate();
        }
    }

    private void deleteSubnota(Connection conn, int idSubnota) throws SQLException {
        String sql = "DELETE FROM Subnota WHERE id_subnota = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSubnota);
            pstmt.executeUpdate();
        }
    }
} 