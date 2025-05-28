package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.universidad.dao.SubnotaDAO;
import com.universidad.dao.impl.SubnotaDAOImpl;
import com.universidad.model.Subnota;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    private TableColumn<SubnotaRow, String> nombreColumnSubnotas;
    @FXML
    private TableColumn<SubnotaRow, Integer> parcialColumnSubnotas;
    @FXML
    private TableColumn<SubnotaRow, Integer> numeroColumnSubnotas;
    @FXML
    private TableColumn<SubnotaRow, Double> valorColumnSubnotas;
    @FXML
    private TableColumn<SubnotaRow, Void> accionesColumnSubnotas;

    private Usuario usuario;

    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
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
        String sql = "SELECT c.id_curso, m.nombre as materia FROM Curso c " +
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
        // Obtener estudiantes del curso
        List<EstudianteItem> estudiantes = new ArrayList<>();
        String sqlEst = "SELECT e.id_estudiante, u.nombre || ' ' || u.apellido as nombre FROM Estudiante e JOIN Usuario u ON e.id_usuario = u.id_usuario JOIN Calificacion c ON c.id_estudiante = e.id_estudiante WHERE c.id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlEst)) {
            pstmt.setInt(1, selectedCurso.getIdCurso());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                estudiantes.add(new EstudianteItem(rs.getInt("id_estudiante"), rs.getString("nombre")));
            }
        } catch (Exception e) {
            showError("Error al cargar estudiantes: " + e.getMessage());
            return;
        }
        if (estudiantes.isEmpty()) {
            showError("No hay estudiantes en este curso.");
            return;
        }
        // Diálogo para agregar subnota
        Dialog<SubnotaInput> dialog = new Dialog<>();
        dialog.setTitle("Agregar Subnota");
        dialog.setHeaderText("Seleccione estudiante y datos de la subnota");
        ComboBox<EstudianteItem> estudianteCombo = new ComboBox<>();
        estudianteCombo.getItems().addAll(estudiantes);
        estudianteCombo.setValue(estudiantes.get(0));
        ComboBox<Integer> parcialCombo = new ComboBox<>();
        parcialCombo.getItems().addAll(1, 2, 3);
        parcialCombo.setValue(1);
        Spinner<Integer> numeroSpinner = new Spinner<>(1, 10, 1);
        Spinner<Double> valorSpinner = new Spinner<>(0.0, 10.0, 0.0, 0.1);
        valorSpinner.setEditable(true);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Estudiante:"), 0, 0); grid.add(estudianteCombo, 1, 0);
        grid.add(new Label("Parcial:"), 0, 1); grid.add(parcialCombo, 1, 1);
        grid.add(new Label("N° Subnota:"), 0, 2); grid.add(numeroSpinner, 1, 2);
        grid.add(new Label("Valor (0-10):"), 0, 3); grid.add(valorSpinner, 1, 3);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Forzar commit del valor escrito en el Spinner
                valorSpinner.getEditor().commitValue();
                String valorText = valorSpinner.getEditor().getText();
                double valor;
                try {
                    valor = Double.parseDouble(valorText);
                } catch (NumberFormatException ex) {
                    showError("El valor de la subnota debe ser un número válido.");
                    return null;
                }
                int parcial = parcialCombo.getValue();
                int numero = numeroSpinner.getValue();
                // Validar rango de subnota según parcial
                if ((parcial == 1 && (numero < 1 || numero > 3)) ||
                    (parcial == 2 && (numero < 4 || numero > 6)) ||
                    (parcial == 3 && (numero < 7 || numero > 10))) {
                    showError("El número de subnota para el parcial " + parcial + " debe ser: " +
                        (parcial == 1 ? "1, 2 o 3" : parcial == 2 ? "4, 5 o 6" : "7, 8, 9 o 10"));
                    return null;
                }
                if (valor < 0 || valor > 10) {
                    showError("El valor de la subnota debe estar entre 0 y 10.");
                    return null;
                }
                return new SubnotaInput(
                    estudianteCombo.getValue(),
                    parcial,
                    numero,
                    valor
                );
            }
            return null;
        });
        Optional<SubnotaInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Buscar id_calificacion
                int idCalificacion = -1;
                String sqlCal = "SELECT id_calificacion FROM Calificacion WHERE id_estudiante = ? AND id_curso = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCal)) {
                    pstmt.setInt(1, input.estudiante.id);
                    pstmt.setInt(2, selectedCurso.getIdCurso());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) idCalificacion = rs.getInt("id_calificacion");
                }
                if (idCalificacion == -1) {
                    showError("No se encontró la calificación para el estudiante seleccionado.");
                    return;
                }
                // Validar máximo 10 subnotas
                SubnotaDAO subnotaDAO = new SubnotaDAOImpl();
                java.util.List<Subnota> subnotas = subnotaDAO.findByCalificacion(idCalificacion);
                if (subnotas.size() >= 10) {
                    showError("No se pueden agregar más de 10 subnotas para esta materia.");
                    return;
                }
                // Validar que no exista ya una subnota con ese número en ese parcial
                for (Subnota s : subnotas) {
                    if (s.getParcial().equals(input.parcial) && s.getNumero().equals(input.numero)) {
                        showError("Ya existe una subnota número " + input.numero + " para el parcial " + input.parcial + ".");
                        return;
                    }
                }
                // Validar suma de subnotas
                double suma = input.valor;
                for (Subnota s : subnotas) {
                    if (s.getParcial().equals(input.parcial)) {
                        suma += s.getValor();
                    }
                }
                if (suma > 100) {
                    showError("La suma de subnotas para este estudiante y parcial no puede superar 100.");
                    return;
                }
                // Crear subnota
                Subnota nueva = new Subnota(null, idCalificacion, input.parcial, input.numero, input.valor);
                subnotaDAO.create(nueva);
                showInfo("Subnota agregada correctamente.");
                // Aquí deberías recargar la tabla de subnotas
            } catch (Exception e) {
                showError("Error al guardar subnota: " + e.getMessage());
            }
        });
    }

    private static class EstudianteItem {
        int id;
        String nombre;
        EstudianteItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
    private static class SubnotaInput {
        EstudianteItem estudiante;
        int parcial;
        int numero;
        double valor;
        SubnotaInput(EstudianteItem estudiante, int parcial, int numero, double valor) {
            this.estudiante = estudiante;
            this.parcial = parcial;
            this.numero = numero;
            this.valor = valor;
        }
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

    public static class EstudianteNotaRow {
        private final String nombre;
        private final Double nota;
        public EstudianteNotaRow(String nombre, Double nota) {
            this.nombre = nombre;
            this.nota = nota;
        }
        public String getNombre() { return nombre; }
        public Double getNota() { return nota; }
    }

    private void loadSubnotas() {
        CursoItem selectedCurso = cursoComboBox.getValue();
        if (selectedCurso == null) {
            subnotasTable.setItems(FXCollections.observableArrayList());
            return;
        }
        ObservableList<SubnotaRow> rows = FXCollections.observableArrayList();
        String sql = "SELECT e.id_estudiante, u.nombre || ' ' || u.apellido as nombre, c.id_calificacion FROM Estudiante e " +
                "JOIN Usuario u ON e.id_usuario = u.id_usuario " +
                "JOIN Calificacion c ON c.id_estudiante = e.id_estudiante " +
                "WHERE c.id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedCurso.getIdCurso());
            ResultSet rs = pstmt.executeQuery();
            SubnotaDAO subnotaDAO = new SubnotaDAOImpl();
            while (rs.next()) {
                int idEst = rs.getInt("id_estudiante");
                String nombre = rs.getString("nombre");
                int idCal = rs.getInt("id_calificacion");
                for (Subnota s : subnotaDAO.findByCalificacion(idCal)) {
                    rows.add(new SubnotaRow(
                        s.getIdSubnota(),
                        idEst,
                        nombre,
                        s.getParcial(),
                        s.getNumero(),
                        s.getValor()
                    ));
                }
            }
            subnotasTable.setItems(rows);
        } catch (Exception e) {
            showError("Error al cargar subnotas: " + e.getMessage());
        }
    }

    private void setupSubnotasTable() {
        nombreColumnSubnotas.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        parcialColumnSubnotas.setCellValueFactory(new PropertyValueFactory<>("parcial"));
        numeroColumnSubnotas.setCellValueFactory(new PropertyValueFactory<>("numero"));
        valorColumnSubnotas.setCellValueFactory(new PropertyValueFactory<>("valor"));
        accionesColumnSubnotas.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");
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
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void showEditSubnotaDialog(SubnotaRow row) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Editar Subnota");
        dialog.setHeaderText("Editar valor de la subnota");
        Spinner<Double> valorSpinner = new Spinner<>(0.0, 10.0, row.valor, 0.1);
        valorSpinner.setEditable(true);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Valor (0-10):"), 0, 0); grid.add(valorSpinner, 1, 0);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Forzar commit del valor escrito en el Spinner
                valorSpinner.getEditor().commitValue();
                String valorText = valorSpinner.getEditor().getText();
                double valor;
                try {
                    valor = Double.parseDouble(valorText);
                } catch (NumberFormatException ex) {
                    showError("El valor de la subnota debe ser un número válido.");
                    return null;
                }
                if (valor < 0 || valor > 10) {
                    showError("El valor de la subnota debe estar entre 0 y 10.");
                    return null;
                }
                return valor;
            }
            return null;
        });
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(valorNuevo -> {
            try {
                if (valorNuevo == null || valorNuevo < 0 || valorNuevo > 10) {
                    showError("El valor de la subnota debe estar entre 0 y 10.");
                    return;
                }
                // Validar máximo 10 subnotas
                SubnotaDAO subnotaDAO = new SubnotaDAOImpl();
                java.util.List<Subnota> subnotas = subnotaDAO.findByCalificacion(row.idCalificacion);
                if (subnotas.size() > 10) {
                    showError("No se pueden tener más de 10 subnotas para esta materia.");
                    return;
                }
                // Validar que no exista ya una subnota con ese número en ese parcial (excepto la actual)
                for (Subnota s : subnotas) {
                    if (s.getParcial().equals(row.parcial) && s.getNumero().equals(row.numero) && !s.getIdSubnota().equals(row.idSubnota)) {
                        showError("Ya existe una subnota número " + row.numero + " para el parcial " + row.parcial + ".");
                        return;
                    }
                }
                // Validar suma de subnotas
                double suma = valorNuevo;
                for (Subnota s : subnotas) {
                    if (s.getParcial().equals(row.parcial) && !s.getIdSubnota().equals(row.idSubnota)) {
                        suma += s.getValor();
                    }
                }
                if (suma > 100) {
                    showError("La suma de subnotas para este estudiante y parcial no puede superar 100.");
                    return;
                }
                // Actualizar subnota
                Subnota subnota = new Subnota(row.idSubnota, row.idCalificacion, row.parcial, row.numero, valorNuevo);
                subnotaDAO.update(subnota);
                showInfo("Subnota actualizada correctamente.");
                loadSubnotas();
            } catch (Exception e) {
                showError("Error al actualizar subnota: " + e.getMessage());
            }
        });
    }

    private void handleDeleteSubnota(SubnotaRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Subnota");
        alert.setHeaderText("¿Está seguro de eliminar esta subnota?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SubnotaDAO subnotaDAO = new SubnotaDAOImpl();
                subnotaDAO.delete(row.idSubnota);
                showInfo("Subnota eliminada correctamente.");
                loadSubnotas();
            } catch (Exception e) {
                showError("Error al eliminar subnota: " + e.getMessage());
            }
        }
    }

    public static class SubnotaRow {
        int idSubnota;
        int idEstudiante;
        String nombre;
        int parcial;
        int numero;
        double valor;
        int idCalificacion;
        public SubnotaRow(int idSubnota, int idEstudiante, String nombre, int parcial, int numero, double valor) {
            this.idSubnota = idSubnota;
            this.idEstudiante = idEstudiante;
            this.nombre = nombre;
            this.parcial = parcial;
            this.numero = numero;
            this.valor = valor;
        }
        public String getNombre() { return nombre; }
        public int getParcial() { return parcial; }
        public int getNumero() { return numero; }
        public double getValor() { return valor; }
    }

    @FXML
    private void handleActualizarSubnotas() {
        loadSubnotas();
    }
} 