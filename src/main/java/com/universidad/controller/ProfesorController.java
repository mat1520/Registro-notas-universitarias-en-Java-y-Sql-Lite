package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
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
        // Obtener id_materia y carrera del curso
        int idMateria = -1;
        int idCarrera = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT m.id_materia, m.id_carrera FROM Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE c.id_curso = ?")) {
            pstmt.setInt(1, selectedCurso.getIdCurso());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idMateria = rs.getInt("id_materia");
                idCarrera = rs.getInt("id_carrera");
            }
        } catch (SQLException e) {
            showError("Database error getting course data: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Unexpected error getting course data: " + e.getMessage());
            return;
        }
        if (idCarrera == -1) {
            showError("Could not determine the career of the course or course not found.");
            return;
        }
        List<EstudianteItem> estudiantes = new ArrayList<>();
        String sqlEst = "SELECT e.id_estudiante, u.nombre_usuario || ' ' || u.apellido_usuario as nombre FROM Estudiante e JOIN Usuario u ON e.id_usuario = u.id_usuario WHERE e.id_carrera = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlEst)) {
            pstmt.setInt(1, idCarrera);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                estudiantes.add(new EstudianteItem(rs.getInt("id_estudiante"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            showError("Database error loading students: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Unexpected error loading students: " + e.getMessage());
            return;
        }
        if (estudiantes.isEmpty()) {
            showError("No students found in the career of this course.");
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
                    // Crear registro en Calificacion
                    String sqlInsertCal = "INSERT INTO Calificacion (id_estudiante, id_curso) VALUES (?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertCal, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, input.estudiante.id);
                        pstmt.setInt(2, selectedCurso.getIdCurso());
                        pstmt.executeUpdate();
                        ResultSet rs = pstmt.getGeneratedKeys();
                        if (rs.next()) idCalificacion = rs.getInt(1);
                    }
                }
                if (idCalificacion == -1) {
                    showError("No se pudo crear la calificación para el estudiante seleccionado.");
                    return;
                }

                // Obtener id_parcial basado en el número de parcial seleccionado
                int idParcial = -1;
                String sqlParcial = "SELECT id_parcial FROM Parcial WHERE Nombre = ?";
                try (PreparedStatement pstmtParcial = conn.prepareStatement(sqlParcial)) {
                    pstmtParcial.setInt(1, input.numeroParcialSeleccionado);
                    ResultSet rsParcial = pstmtParcial.executeQuery();
                    if (rsParcial.next()) {
                        idParcial = rsParcial.getInt("id_parcial");
                    }
                } catch (SQLException e) {
                    showError("Database error getting parcial ID: " + e.getMessage());
                    return;
                }

                if (idParcial == -1) {
                     showError("Could not find the corresponding parcial in the database.");
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
                    if (s.getIdParcialBaseDatos() == idParcial && s.getNumero() == input.numero) {
                        showError("Ya existe una subnota número " + input.numero + " para el parcial " + input.numeroParcialSeleccionado + ".");
                        return;
                    }
                }
                // Validar suma de subnotas
                double suma = input.valor;
                for (Subnota s : subnotas) {
                    if (s.getIdParcialBaseDatos() == idParcial) { // Compare with id_parcial
                        suma += s.getValor();
                    }
                }
                if (suma > 100) {
                    showError("La suma de subnotas para este estudiante y parcial no puede superar 100.");
                    return;
                }
                // Crear subnota
                // Use the fetched idParcial and the input.numero (subnota number)
                Subnota nueva = new Subnota(null, idCalificacion, idParcial, input.numero, input.valor);
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
        private String nombre_completo;

        EstudianteItem(int id, String nombre_completo) {
            this.id = id;
            this.nombre_completo = nombre_completo;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return nombre_completo; }
    }
    private static class SubnotaInput {
        EstudianteItem estudiante;
        int numeroParcialSeleccionado;
        int numero;
        double valor;
        SubnotaInput(EstudianteItem estudiante, int numeroParcialSeleccionado, int numero, double valor) {
            this.estudiante = estudiante;
            this.numeroParcialSeleccionado = numeroParcialSeleccionado;
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
        String sql = "SELECT sn.id_subnota, p.Nombre as parcial_nombre, sn.Numero as subnota_numero, sn.valor, sn.id_calificacion, e.id_estudiante, u.nombre_usuario || ' ' || u.apellido_usuario as nombre_completo, sn.id_parcial " +
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
                    rs.getInt("id_parcial"), // Almacena id_parcial
                    rs.getInt("subnota_numero"), // Almacena número de subnota
                    rs.getDouble("valor"),
                    rs.getInt("id_calificacion"),
                    rs.getString("parcial_nombre") // Almacena nombre del parcial
                ));
            }
            subnotasTable.setItems(rows);
        } catch (Exception e) {
            showError("Error al cargar subnotas: " + e.getMessage());
        }
    }

    private void setupSubnotasTable() {
        estudianteColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_completo"));
        parcialColumn.setCellValueFactory(new PropertyValueFactory<>("parcial_nombre")); // Mapear a la propiedad correcta
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero")); // Mapear a la propiedad correcta para el número de subnota
        valorColumn.setCellValueFactory(new PropertyValueFactory<>("valor"));
        accionesColumn.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Editar");
            private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Eliminar");
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
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void showEditSubnotaDialog(SubnotaRow row) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Editar Subnota");
        dialog.setHeaderText("Editar valor de la subnota para " + row.getNombre_completo() + " (Parcial: " + row.getParcial_nombre() + ", N°: " + row.getNumero() + ")");
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
                    if (s.getIdParcialBaseDatos() == row.id_parcial_db && s.getNumero() == row.numero && !s.getIdSubnota().equals(row.idSubnota)) {
                        showError("Ya existe una subnota número " + row.numero + " para el parcial " + row.parcial_nombre + ".");
                        return;
                    }
                }
                // Validar suma de subnotas
                double suma = valorNuevo;
                for (Subnota s : subnotas) {
                    if (s.getIdParcialBaseDatos() == row.id_parcial_db && !s.getIdSubnota().equals(row.idSubnota)) {
                        suma += s.getValor();
                    }
                }
                if (suma > 100) {
                    showError("La suma de subnotas para este estudiante y parcial no puede superar 100.");
                    return;
                }
                // Actualizar subnota
                // Usar id_parcial_db almacenado en la fila
                Subnota subnota = new Subnota(row.idSubnota, row.idCalificacion, row.id_parcial_db, row.numero, valorNuevo);
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
        alert.setHeaderText("¿Está seguro de eliminar la subnota para " + row.getNombre_completo() + " (Parcial: " + row.getParcial_nombre() + ", N°: " + row.getNumero() + ")?");
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
        String nombre_completo;
        int id_parcial_db; // Renombrar para mayor claridad (almacena id_parcial de la base de datos)
        int numero; // Este campo almacenará el número de subnota (1-10) de la base de datos (columna Numero)
        double valor;
        int idCalificacion;
        String parcial_nombre; // Campo para el nombre del parcial (de la columna Nombre en Parcial)

        public SubnotaRow(int idSubnota, int idEstudiante, String nombre_completo, int id_parcial_db, int numero, double valor, int idCalificacion, String parcial_nombre) {
            this.idSubnota = idSubnota;
            this.idEstudiante = idEstudiante;
            this.nombre_completo = nombre_completo;
            this.id_parcial_db = id_parcial_db; // Almacena id_parcial
            this.numero = numero; // Almacena número de subnota
            this.valor = valor;
            this.idCalificacion = idCalificacion;
            this.parcial_nombre = parcial_nombre; // Almacena nombre del parcial
        }

        public int getIdSubnota() { return idSubnota; }
        public int getIdEstudiante() { return idEstudiante; }
        public String getNombre_completo() { return nombre_completo; }
        public int getId_parcial_db() { return id_parcial_db; } // Nuevo getter para id_parcial de la base de datos
        public int getNumero() { return numero; } // Devuelve número de subnota
        public double getValor() { return valor; }
        public int getIdCalificacion() { return idCalificacion; }
        public String getParcial_nombre() { return parcial_nombre; } // Getter para nombre del parcial
    }

    @FXML
    private void handleActualizarSubnotas() {
        loadSubnotas(); // Recargar la tabla después de actualizar
    }
} 