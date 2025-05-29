package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.universidad.model.Usuario;
import com.universidad.util.DatabaseConnection;

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
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Tab;
import javafx.scene.control.SelectionMode;

public class AdminController implements MainController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private TableView<UsuarioRow> usuariosTable;
    
    @FXML
    private TableColumn<UsuarioRow, Integer> idColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> cedulaColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> nombreColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> apellidoColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> rolColumn;
    
    @FXML
    private Button agregarUsuarioButton;
    
    @FXML
    private TextField cedulaField;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField apellidoField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> rolCombo;

    @FXML
    private ComboBox<CarreraItem> carreraCombo;

    @FXML
    private ListView<MateriaItem> materiasList;
    
    @FXML
    private TableView<MateriaRow> materiasTable;
    
    @FXML
    private TableColumn<MateriaRow, Integer> idMateriaColumn;
    
    @FXML
    private TableColumn<MateriaRow, String> nombreMateriaColumn;
    
    @FXML
    private TableColumn<MateriaRow, String> carreraMateriaColumn;
    
    @FXML
    private Button agregarMateriaButton;
    
    @FXML
    private TableView<CarreraRow> carrerasTable;
    
    @FXML
    private TableColumn<CarreraRow, Integer> idCarreraColumn;
    
    @FXML
    private TableColumn<CarreraRow, String> nombreCarreraColumn;
    
    @FXML
    private Button agregarCarreraButton;
    
    private Usuario usuario;
    
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre_usuario() + " " + usuario.getApellido_usuario());
        loadUsuarios();
    }
    
    @FXML
    private void initialize() {
        cedulaColumn.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_usuario"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido_usuario"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));
        if (usuariosTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        
        // Inicializar pestaña de materias
        if (materiasTable != null) {
            idMateriaColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nombreMateriaColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_materia"));
            carreraMateriaColumn.setCellValueFactory(new PropertyValueFactory<>("carrera_nombre"));
            refreshMateriasTable();
        }

        if (carrerasTable != null) {
            idCarreraColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nombreCarreraColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_carrera"));
            refreshCarrerasTable();
        }
    }
    
    private void loadUsuarios() {
        refreshTable();
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Gestión de Notas - Login");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.hide();
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            showError("Error al cerrar sesión: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAgregarUsuario() {
        Dialog<UsuarioInput> dialog = new Dialog<>();
        dialog.setTitle("Agregar Usuario");
        dialog.setHeaderText("Ingrese los datos del nuevo usuario");
        TextField cedulaField = new TextField();
        TextField nombreField = new TextField();
        TextField apellidoField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button generarPassBtn = new Button("Generar contraseña segura");
        generarPassBtn.setOnAction(ev -> {
            String pass = generarPasswordSegura();
            passwordField.setText(pass);
            ClipboardContent content = new ClipboardContent();
            content.putString(pass);
            Clipboard.getSystemClipboard().setContent(content);
            showInfo("Contraseña generada y copiada al portapapeles.");
        });
        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        rolComboBox.setValue("ESTUDIANTE");
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        // Cargar carreras dinámicamente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar carreras: " + e.getMessage());
        }
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(120);
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre_materia FROM Materia WHERE id_carrera = ? ORDER BY nombre_materia")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException e) {
                    showError("Error al cargar materias: " + e.getMessage());
                } catch (Exception e) {
                    showError("Error inesperado al cargar materias: " + e.getMessage());
                }
            }
        });
        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int layoutRow = 0;
        grid.add(new Label("Cédula:"), 0, layoutRow); grid.add(cedulaField, 1, layoutRow++);
        grid.add(new Label("Nombre:"), 0, layoutRow); grid.add(nombreField, 1, layoutRow++);
        grid.add(new Label("Apellido:"), 0, layoutRow); grid.add(apellidoField, 1, layoutRow++);
        grid.add(new Label("Contraseña:"), 0, layoutRow); grid.add(passwordField, 1, layoutRow);
        grid.add(generarPassBtn, 2, layoutRow++);
        grid.add(new Label("Rol:"), 0, layoutRow); grid.add(rolComboBox, 1, layoutRow++);
        Label carreraLabel = new Label("Carrera:");
        grid.add(carreraLabel, 0, layoutRow); grid.add(carreraComboBox, 1, layoutRow++);
        Label materiasLabel = new Label("Materias:");
        grid.add(materiasLabel, 0, layoutRow); grid.add(materiasListView, 1, layoutRow++);
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabel.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasLabel.setVisible(esEstudiante || esProfesor);
            materiasListView.setVisible(esEstudiante || esProfesor);
            if (esEstudiante) {
                carreraComboBox.getOnAction().handle(null);
            } else if (esProfesor) {
                materiasListView.getItems().clear();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre_materia FROM Materia ORDER BY nombre_materia")) {
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException e) {
                    // Puede no haber materias, no es crítico
                } catch (Exception e) {
                    showError("Error inesperado al cargar materias: " + e.getMessage());
                }
            }
        });
        rolComboBox.getOnAction().handle(null);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cedula = cedulaField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String password = passwordField.getText();
                String rol = rolComboBox.getValue();
                CarreraItem carrera = carreraComboBox.getValue();
                var materiasSeleccionadas = materiasListView.getSelectionModel().getSelectedItems();
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || rol == null) {
                    showError("Todos los campos obligatorios deben estar llenos.");
                    return null;
                }
                if (!cedula.matches("\\d{10}")) {
                    showError("La cédula debe tener exactamente 10 dígitos numéricos.");
                    return null;
                }
                if (!validarCedulaEcuatoriana(cedula)) {
                    showError("Cédula ecuatoriana no válida.");
                    return null;
                }
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ ]+")) {
                    showError("El nombre solo puede contener letras y espacios.");
                    return null;
                }
                if (!apellido.matches("[A-Za-záéíóúÁÉÍÓÚñÑ ]+")) {
                    showError("El apellido solo puede contener letras y espacios.");
                    return null;
                }
                if (password != null && !password.isEmpty() && password.length() < 6) {
                    showError("La contraseña debe tener al menos 6 caracteres.");
                    return null;
                }
                if ("ESTUDIANTE".equals(rol) && carrera == null) {
                    showError("Debe seleccionar una carrera para el estudiante.");
                    return null;
                }
                if ("PROFESOR".equals(rol)) {
                    for (MateriaItem materia : materiasSeleccionadas) {
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(
                                 "SELECT cu.id_profesor FROM Curso cu WHERE cu.id_materia = ?")) {
                            pstmt.setInt(1, materia.getId());
                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next()) {
                                showError("La materia '" + materia.getNombre_materia() + "' ya está asignada a otro profesor.");
                                return null;
                            }
                        } catch (SQLException e) {
                            showError("Error al validar materias: " + e.getMessage());
                            return null;
                        } catch (Exception e) {
                            showError("Error inesperado al validar materias: " + e.getMessage());
                            return null;
                        }
                    }
                }
                return new UsuarioInput(
                    cedula, nombre, apellido, 
                    password,
                    rol, carrera,
                    "ESTUDIANTE".equals(rol) ? new java.util.ArrayList<MateriaItem>(materiasSeleccionadas) : null,
                    "PROFESOR".equals(rol) ? new java.util.ArrayList<MateriaItem>(materiasSeleccionadas) : null
                );
            }
            return null;
        });
        Optional<UsuarioInput> result = dialog.showAndWait();
        result.ifPresent(input -> agregarUsuario(input));
    }

    private void agregarUsuario(UsuarioInput input) {
        Connection conn = null; // Declare connection outside try block
        try {
            conn = DatabaseConnection.getConnection(); // Get the connection
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO Usuario (cedula, nombre_usuario, apellido_usuario, password, rol) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, input.getCedula());
                pstmt.setString(2, input.getNombre_usuario());
                pstmt.setString(3, input.getApellido_usuario());
                pstmt.setString(4, input.getPassword());
                pstmt.setString(5, input.getRol());
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int idUsuario = rs.getInt(1);
                    if ("ESTUDIANTE".equals(input.getRol()) && input.getCarrera() != null) {
                        String sqlEst = "INSERT INTO Estudiante (id_usuario, id_carrera) VALUES (?, ?)";
                        try (PreparedStatement pstmtEst = conn.prepareStatement(sqlEst, Statement.RETURN_GENERATED_KEYS)) {
                            pstmtEst.setInt(1, idUsuario);
                            pstmtEst.setInt(2, input.getCarrera().getId());
                            pstmtEst.executeUpdate();
                            ResultSet rsEst = pstmtEst.getGeneratedKeys();
                            int idEstudiante = rsEst.next() ? rsEst.getInt(1) : -1;
                            if (input.getMateriasEstudiante() != null) {
                                for (MateriaItem materia : input.getMateriasEstudiante()) {
                                    // Crear calificaciones para todos los cursos de esa materia
                                    String sqlCursos = "SELECT id_curso FROM Curso WHERE id_materia = ?";
                                    try (PreparedStatement pstmtCursos = conn.prepareStatement(sqlCursos)) {
                                        pstmtCursos.setInt(1, materia.getId());
                                        ResultSet rsCursos = pstmtCursos.executeQuery();
                                        while (rsCursos.next()) {
                                            int idCurso = rsCursos.getInt("id_curso");
                                            String sqlCal = "INSERT OR IGNORE INTO Calificacion (id_estudiante, id_curso) VALUES (?, ?)";
                                            try (PreparedStatement pstmtCal = conn.prepareStatement(sqlCal)) {
                                                pstmtCal.setInt(1, idEstudiante);
                                                pstmtCal.setInt(2, idCurso);
                                                pstmtCal.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if ("PROFESOR".equals(input.getRol()) && input.getMateriasProfesor() != null) {
                        String sqlProf = "INSERT INTO Profesor (id_usuario, nombre_profesor) VALUES (?, ?)";
                        try (PreparedStatement pstmtProf = conn.prepareStatement(sqlProf, Statement.RETURN_GENERATED_KEYS)) {
                            pstmtProf.setInt(1, idUsuario);
                            pstmtProf.setString(2, input.getNombre_usuario() + " " + input.getApellido_usuario());
                            pstmtProf.executeUpdate();
                            ResultSet rsProf = pstmtProf.getGeneratedKeys();
                            int idProfesor = rsProf.next() ? rsProf.getInt(1) : -1;
                            if (idProfesor == -1) {
                                throw new SQLException("Could not retrieve generated id_profesor after insertion.");
                            }
                            for (MateriaItem materia : input.getMateriasProfesor()) {
                                String sqlCurso = "INSERT INTO Curso (id_materia, id_profesor, periodo, seccion, cupo) VALUES (?, ?, '2024-01', 'A', 30)";
                                try (PreparedStatement pstmtCurso = conn.prepareStatement(sqlCurso)) {
                                    pstmtCurso.setInt(1, materia.getId());
                                    pstmtCurso.setInt(2, idProfesor);
                                    pstmtCurso.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }

            conn.commit(); // Commit the transaction
            showSuccess("Usuario agregado correctamente.");
            refreshTable();

        } catch (SQLException e) {
            // Attempt rollback if transaction is active
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    showError("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            // Check for unique constraint violation on 'cedula'
            if (e.getErrorCode() == 19 && e.getMessage().contains("UNIQUE constraint failed: Usuario.cedula")) {
                showError("Error al agregar usuario: El número de cédula ya está registrado.");
            } else {
                // For other SQL errors
                showError("Error al agregar usuario (SQL): " + e.getMessage());
            }
        } catch (Exception e) {
            // Attempt rollback for other exceptions
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    showError("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            // For other unexpected errors
            showError("Error inesperado al agregar usuario: " + e.getMessage());
        } finally {
            // Ensure connection is closed
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    showError("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleEditarUsuario() {
        UsuarioRow selected = usuariosTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione un usuario para editar.");
            return;
        }
        showEditDialog(selected);
    }

    private void showEditDialog(UsuarioRow row) {
        Dialog<UsuarioInput> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Editar información de " + row.getNombre_usuario() + " " + row.getApellido_usuario());

        TextField cedulaField = new TextField(row.getCedula());
        TextField nombreField = new TextField(row.getNombre_usuario());
        TextField apellidoField = new TextField(row.getApellido_usuario());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Dejar en blanco para mantener la contraseña actual");
        Button generarPassBtn = new Button("Generar contraseña segura");
        generarPassBtn.setOnAction(ev -> {
            String pass = generarPasswordSegura();
            passwordField.setText(pass);
            ClipboardContent content = new ClipboardContent();
            content.putString(pass);
            Clipboard.getSystemClipboard().setContent(content);
            showInfo("Contraseña generada y copiada al portapapeles.");
        });
        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        rolComboBox.setValue(row.getRol());
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar carreras: " + e.getMessage());
        }
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(120);
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre_materia FROM Materia WHERE id_carrera = ? ORDER BY nombre_materia")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException e) {
                    showError("Error al cargar materias: " + e.getMessage());
                } catch (Exception e) {
                    showError("Error inesperado al cargar materias: " + e.getMessage());
                }
            }
        });
        Label carreraLabel = new Label("Carrera:");
        Label materiasLabel = new Label("Materias:");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int layoutRow = 0;
        grid.add(new Label("Cédula:"), 0, layoutRow); grid.add(cedulaField, 1, layoutRow++);
        grid.add(new Label("Nombre:"), 0, layoutRow); grid.add(nombreField, 1, layoutRow++);
        grid.add(new Label("Apellido:"), 0, layoutRow); grid.add(apellidoField, 1, layoutRow++);
        grid.add(new Label("Contraseña:"), 0, layoutRow); grid.add(passwordField, 1, layoutRow);
        grid.add(generarPassBtn, 2, layoutRow++);
        grid.add(new Label("Rol:"), 0, layoutRow); grid.add(rolComboBox, 1, layoutRow++);
        grid.add(carreraLabel, 0, layoutRow); grid.add(carreraComboBox, 1, layoutRow++);
        grid.add(materiasLabel, 0, layoutRow); grid.add(materiasListView, 1, layoutRow++);
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabel.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasLabel.setVisible(esEstudiante || esProfesor);
            materiasListView.setVisible(esEstudiante || esProfesor);
            if (esEstudiante) {
                carreraComboBox.getOnAction().handle(null);
            } else if (esProfesor) {
                materiasListView.getItems().clear();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre_materia FROM Materia ORDER BY nombre_materia")) {
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException e) {
                    // Puede no haber materias, no es crítico
                } catch (Exception e) {
                    showError("Error inesperado al cargar materias: " + e.getMessage());
                }
            }
        });
        rolComboBox.getOnAction().handle(null);
        // Cargar datos actuales
        if ("ESTUDIANTE".equals(row.getRol())) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT e.id_carrera FROM Estudiante e WHERE e.id_usuario = ?")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int idCarrera = rs.getInt("id_carrera");
                    for (CarreraItem carrera : carreraComboBox.getItems()) {
                        if (carrera.getId() == idCarrera) {
                            carreraComboBox.setValue(carrera);
                            break;
                        }
                    }
                    carreraComboBox.getOnAction().handle(null);
                }
            } catch (SQLException e) {
                showError("Error al cargar carrera del estudiante: " + e.getMessage());
            } catch (Exception e) {
                showError("Error inesperado al cargar carrera del estudiante: " + e.getMessage());
            }
            // Seleccionar materias actuales (si aplica)
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT m.id_materia FROM Materia m JOIN Calificacion c ON m.id_materia = c.id_materia JOIN Estudiante e ON c.id_estudiante = e.id_estudiante JOIN Curso cu ON c.id_curso = cu.id_curso WHERE e.id_usuario = ? AND cu.id_materia = m.id_materia")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idMateria = rs.getInt("id_materia");
                    for (MateriaItem materia : materiasListView.getItems()) {
                        if (materia.getId() == idMateria) {
                            materiasListView.getSelectionModel().select(materia);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                // Puede no haber materias, no es crítico
            } catch (Exception e) {
                showError("Error inesperado al cargar materias del estudiante: " + e.getMessage());
            }
        } else if ("PROFESOR".equals(row.getRol())) {
            // Cargar todas las materias
            materiasListView.getItems().clear();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id_materia, nombre_materia FROM Materia ORDER BY nombre_materia")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                }
            } catch (SQLException e) {
                // Puede no haber materias, no es crítico
            } catch (Exception e) {
                showError("Error inesperado al cargar todas las materias: " + e.getMessage());
            }
            // Seleccionar las materias que ya imparte
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT m.id_materia FROM Materia m JOIN Curso c ON m.id_materia = c.id_materia JOIN Profesor p ON c.id_profesor = p.id_profesor WHERE p.id_usuario = ?")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idMateria = rs.getInt("id_materia");
                    for (MateriaItem materia : materiasListView.getItems()) {
                        if (materia.getId() == idMateria) {
                            materiasListView.getSelectionModel().select(materia);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                // Puede no haber materias, no es crítico
            } catch (Exception e) {
                showError("Error inesperado al cargar materias del profesor: " + e.getMessage());
            }
        }
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cedula = cedulaField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String password = passwordField.getText().trim();
                String rol = rolComboBox.getValue();
                CarreraItem carrera = carreraComboBox.getValue();
                List<MateriaItem> materiasSeleccionadas = new ArrayList<>(materiasListView.getSelectionModel().getSelectedItems());
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || rol == null) {
                    showError("Todos los campos obligatorios deben estar llenos.");
                    return null;
                }
                if (!cedula.matches("\\d{10}")) {
                    showError("La cédula debe tener exactamente 10 dígitos numéricos.");
                    return null;
                }
                if (!validarCedulaEcuatoriana(cedula)) {
                    showError("Cédula ecuatoriana no válida.");
                    return null;
                }
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ ]+")) {
                    showError("El nombre solo puede contener letras y espacios.");
                    return null;
                }
                if (!apellido.matches("[A-Za-záéíóúÁÉÍÓÚñÑ ]+")) {
                    showError("El apellido solo puede contener letras y espacios.");
                    return null;
                }
                if (!password.isEmpty() && password.length() < 6) {
                    showError("La contraseña debe tener al menos 6 caracteres.");
                    return null;
                }
                // Validar unicidad de cédula (excluyendo el usuario actual)
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM Usuario WHERE cedula = ? AND id_usuario != ?")) {
                    pstmt.setString(1, cedula);
                    pstmt.setInt(2, row.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe otro usuario con esta cédula.");
                        return null;
                    }
                } catch (SQLException e) {
                    showError("Error al validar la cédula: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    showError("Error inesperado al validar la cédula: " + e.getMessage());
                    return null;
                }
                if ("ESTUDIANTE".equals(rol) && carrera == null) {
                    showError("Debe seleccionar una carrera para el estudiante.");
                    return null;
                }
                if ("PROFESOR".equals(rol)) {
                    for (MateriaItem materia : materiasSeleccionadas) {
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(
                                 "SELECT cu.id_profesor, p.id_usuario FROM Curso cu JOIN Profesor p ON cu.id_profesor = p.id_profesor WHERE cu.id_materia = ?")) {
                            pstmt.setInt(1, materia.getId());
                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next()) {
                                int idUsuarioAsignado = rs.getInt("id_usuario");
                                if (idUsuarioAsignado != row.getId()) {
                                    showError("La materia '" + materia.getNombre_materia() + "' ya está asignada a otro profesor.");
                                    return null;
                                }
                            }
                        } catch (SQLException e) {
                            showError("Error al validar materias: " + e.getMessage());
                            return null;
                        } catch (Exception e) {
                            showError("Error inesperado al validar materias: " + e.getMessage());
                            return null;
                        }
                    }
                }
                return new UsuarioInput(
                    cedula, nombre, apellido, 
                    password,
                    rol, carrera,
                    "ESTUDIANTE".equals(rol) ? new ArrayList<>(materiasSeleccionadas) : null,
                    "PROFESOR".equals(rol) ? new ArrayList<>(materiasSeleccionadas) : null
                );
            }
            return null;
        });
        Optional<UsuarioInput> result = dialog.showAndWait();
        result.ifPresent(input -> updateUsuario(row, input));
    }

    private void updateUsuario(UsuarioRow row, UsuarioInput input) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Actualizar datos básicos del usuario
                String sql;
                if (input.getPassword() != null && !input.getPassword().isEmpty()) {
                    // Update with password
                    sql = "UPDATE Usuario SET cedula = ?, nombre_usuario = ?, apellido_usuario = ?, password = ?, rol = ? WHERE id_usuario = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, input.getCedula());
                        pstmt.setString(2, input.getNombre_usuario());
                        pstmt.setString(3, input.getApellido_usuario());
                        pstmt.setString(4, input.getPassword());
                        pstmt.setString(5, input.getRol());
                        pstmt.setInt(6, row.getId());
                        pstmt.executeUpdate();
                    }
                } else {
                    // Update without password
                    sql = "UPDATE Usuario SET cedula = ?, nombre_usuario = ?, apellido_usuario = ?, rol = ? WHERE id_usuario = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, input.getCedula());
                        pstmt.setString(2, input.getNombre_usuario());
                        pstmt.setString(3, input.getApellido_usuario());
                        pstmt.setString(4, input.getRol());
                        pstmt.setInt(5, row.getId());
                        pstmt.executeUpdate();
                    }
                }

                // Si es profesor, actualizar materias a calificar
                if ("PROFESOR".equals(input.getRol())) {
                    int idProfesor = -1;
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_profesor FROM Profesor WHERE id_usuario = ?")) {
                        pstmt.setInt(1, row.getId());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) idProfesor = rs.getInt(1);
                    }

                    if (idProfesor != -1) {
                        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Curso WHERE id_profesor = ?")) {
                            pstmt.setInt(1, idProfesor);
                            pstmt.executeUpdate();
                        }

                        if (input.getMateriasProfesor() != null && !input.getMateriasProfesor().isEmpty()) {
                            String sqlCurso = "INSERT INTO Curso (id_materia, id_profesor, periodo, seccion, cupo) VALUES (?, ?, '2024-01', 'A', 30)";
                            try (PreparedStatement pstmtCurso = conn.prepareStatement(sqlCurso)) {
                                for (MateriaItem materia : input.getMateriasProfesor()) {
                                    pstmtCurso.setInt(1, materia.getId());
                                    pstmtCurso.setInt(2, idProfesor);
                                    pstmtCurso.executeUpdate();
                                }
                            }
                        }
                    }
                }

                // Actualizar la tabla
                refreshTable();

                conn.commit();
                showInfo("Usuario actualizado correctamente.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            showError("Error al actualizar usuario: " + e.getMessage());
        }
    }
    
    private void showDeleteConfirmation(UsuarioRow row) {
        // Si es admin, verificar cuántos admins hay
        if ("ADMIN".equals(row.getRol())) {
            int adminCount = 0;
            for (UsuarioRow u : usuariosTable.getItems()) {
                if ("ADMIN".equals(u.getRol())) adminCount++;
            }
            if (adminCount <= 1) {
                showError("No se puede eliminar el último usuario administrador.");
                return;
            }
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar a " + row.getNombre_usuario() + " " + row.getApellido_usuario() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteUsuario(row);
        }
    }
    
    private void deleteUsuario(UsuarioRow row) {
        String sql = "DELETE FROM Usuario WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, row.getId());
            pstmt.executeUpdate();
            
            // Actualizar la tabla
            usuariosTable.getItems().remove(row);
            
        } catch (Exception e) {
            showError("Error al eliminar usuario: " + e.getMessage());
        }
    }

    // Validación de cédula ecuatoriana
    private boolean validarCedulaEcuatoriana(String cedula) {
        if (cedula == null || cedula.length() != 10) return false;
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) return false;
        int[] coef = {2,1,2,1,2,1,2,1,2};
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int val = Character.getNumericValue(cedula.charAt(i)) * coef[i];
            if (val > 9) val -= 9;
            suma += val;
        }
        int digitoVerificador = (10 - (suma % 10)) % 10;
        return digitoVerificador == Character.getNumericValue(cedula.charAt(9));
    }

    // Clases auxiliares para el formulario
    public static class CarreraItem {
        private int id;
        private String nombre;

        public CarreraItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static class MateriaItem {
        private int id;
        private String nombre_materia;

        public MateriaItem(int id, String nombre_materia) {
            this.id = id;
            this.nombre_materia = nombre_materia;
        }

        public int getId() {
            return id;
        }

        public String getNombre_materia() {
            return nombre_materia;
        }

        @Override
        public String toString() {
            return nombre_materia;
        }
    }

    public static class UsuarioInput {
        private final String cedula, nombre_usuario, apellido_usuario, password, rol;
        private final CarreraItem carrera;
        private final java.util.List<MateriaItem> materiasEstudiante;
        private final java.util.List<MateriaItem> materiasProfesor;
        public UsuarioInput(String cedula, String nombre_usuario, String apellido_usuario, String password, String rol, CarreraItem carrera, java.util.List<MateriaItem> materiasEstudiante, java.util.List<MateriaItem> materiasProfesor) {
            this.cedula = cedula; this.nombre_usuario = nombre_usuario; this.apellido_usuario = apellido_usuario; this.password = password; this.rol = rol; this.carrera = carrera;
            this.materiasEstudiante = materiasEstudiante == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasEstudiante);
            this.materiasProfesor = materiasProfesor == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasProfesor);
        }
        public String getCedula() { return cedula; }
        public String getNombre_usuario() { return nombre_usuario; }
        public String getApellido_usuario() { return apellido_usuario; }
        public String getPassword() { return password; }
        public String getRol() { return rol; }
        public CarreraItem getCarrera() { return carrera; }
        public java.util.List<MateriaItem> getMateriasEstudiante() { return materiasEstudiante; }
        public java.util.List<MateriaItem> getMateriasProfesor() { return materiasProfesor; }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class UsuarioRow {
        private int id;
        private String cedula;
        private String nombre_usuario;
        private String apellido_usuario;
        private String rol;
        private String carrera;
        private String materias;

        public UsuarioRow(int id, String cedula, String nombre_usuario, String apellido_usuario, String rol, String carrera, String materias) {
            this.id = id;
            this.cedula = cedula;
            this.nombre_usuario = nombre_usuario;
            this.apellido_usuario = apellido_usuario;
            this.rol = rol;
            this.carrera = carrera;
            this.materias = materias;
        }

        public int getId() {
            return id;
        }

        public String getCedula() {
            return cedula;
        }

        public String getNombre_usuario() {
            return nombre_usuario;
        }

        public String getApellido_usuario() {
            return apellido_usuario;
        }

        public String getRol() {
            return rol;
        }

        public String getCarrera() {
            return carrera;
        }

        public String getMaterias() {
            return materias;
        }
    }

    // Método para generar contraseña segura
    private String generarPasswordSegura() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()_+=-{}[]|:;<>?,./";
        String all = upper + lower + digits + symbols;
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        // Garantizar al menos un carácter de cada tipo
        sb.append(upper.charAt(rnd.nextInt(upper.length())));
        sb.append(lower.charAt(rnd.nextInt(lower.length())));
        sb.append(digits.charAt(rnd.nextInt(digits.length())));
        sb.append(symbols.charAt(rnd.nextInt(symbols.length())));
        // Rellenar hasta 12+ caracteres
        for (int i = 4; i < 14; i++) sb.append(all.charAt(rnd.nextInt(all.length())));
        // Mezclar
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }

    private void refreshMateriasTable() {
        if (materiasTable == null) return;
        materiasTable.getItems().clear();
        String sql = "SELECT m.id_materia, m.nombre_materia, c.nombre_carrera FROM Materia m JOIN Carrera c ON m.id_carrera = c.id_carrera ORDER BY c.nombre_carrera, m.nombre_materia";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                materiasTable.getItems().add(new MateriaRow(
                    rs.getInt("id_materia"),
                    rs.getString("nombre_materia"),
                    rs.getString("nombre_carrera")
                ));
            }
        } catch (SQLException e) {
            showError("Error al cargar materias: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar materias: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgregarMateria() {
        Dialog<MateriaInput> dialog = new Dialog<>();
        dialog.setTitle("Agregar Materia");
        dialog.setHeaderText("Ingrese los datos de la nueva materia");
        TextField nombreField = new TextField();
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        // Cargar carreras dinámicamente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar carreras: " + e.getMessage());
        }
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre de la materia:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(new Label("Carrera:"), 0, 1); grid.add(carreraComboBox, 1, 1);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String nombre = nombreField.getText().trim();
                CarreraItem carrera = carreraComboBox.getValue();
                if (nombre.isEmpty() || carrera == null) {
                    showError("Todos los campos son obligatorios.");
                    return null;
                }
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ0-9 ]+")) {
                    showError("El nombre solo puede contener letras, números y espacios.");
                    return null;
                }
                // Validar duplicado
                String sql = "SELECT COUNT(*) FROM Materia WHERE nombre_materia = ? AND id_carrera = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, carrera.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe una materia con ese nombre en la carrera seleccionada.");
                        return null;
                    }
                } catch (SQLException e) {
                    showError("Error al validar materia: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    showError("Error inesperado al validar materia: " + e.getMessage());
                    return null;
                }
                return new MateriaInput(nombre, carrera);
            }
            return null;
        });
        Optional<MateriaInput> result = dialog.showAndWait();
        result.ifPresent(input -> agregarMateria(input));
    }

    private void agregarMateria(MateriaInput input) {
        String sql = "INSERT INTO Materia (nombre_materia, id_carrera) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre_materia());
            pstmt.setInt(2, input.getCarrera().getId());
            pstmt.executeUpdate();
            showSuccess("Materia agregada correctamente.");
            refreshMateriasTable();
        } catch (SQLException e) {
            showError("Error al agregar materia: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al agregar materia: " + e.getMessage());
        }
    }

    // Clase auxiliar para la tabla de materias
    public static class MateriaRow {
        private int id;
        private String nombre_materia;
        private String carrera_nombre;
        public MateriaRow(int id, String nombre_materia, String carrera_nombre) {
            this.id = id; this.nombre_materia = nombre_materia; this.carrera_nombre = carrera_nombre;
        }
        public int getId() { return id; }
        public String getNombre_materia() { return nombre_materia; }
        public String getCarrera_nombre() { return carrera_nombre; }
    }

    public static class MateriaInput {
        private final String nombre_materia;
        private final CarreraItem carrera;
        public MateriaInput(String nombre_materia, CarreraItem carrera) {
            this.nombre_materia = nombre_materia; this.carrera = carrera;
        }
        public String getNombre_materia() { return nombre_materia; }
        public CarreraItem getCarrera() { return carrera; }
    }

    @FXML
    private void handleEditarMateria() {
        MateriaRow selected = materiasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione una materia para editar.");
            return;
        }
        Dialog<MateriaInput> dialog = new Dialog<>();
        dialog.setTitle("Editar Materia");
        dialog.setHeaderText("Editar datos de la materia");
        TextField nombreField = new TextField(selected.getNombre_materia());
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        // Cargar carreras dinámicamente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar carreras: " + e.getMessage());
        }
        carreraComboBox.setValue(carreraComboBox.getItems().stream().filter(c -> c.getNombre().equals(selected.getCarrera_nombre())).findFirst().orElse(null));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre de la materia:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(new Label("Carrera:"), 0, 1); grid.add(carreraComboBox, 1, 1);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String nombre = nombreField.getText().trim();
                CarreraItem carrera = carreraComboBox.getValue();
                if (nombre.isEmpty() || carrera == null) {
                    showError("Todos los campos son obligatorios.");
                    return null;
                }
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ0-9 ]+")) {
                    showError("El nombre solo puede contener letras, números y espacios.");
                    return null;
                }
                // Validar duplicado (excluyendo la materia actual)
                String sql = "SELECT COUNT(*) FROM Materia WHERE nombre_materia = ? AND id_carrera = ? AND id_materia != ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, carrera.getId());
                    pstmt.setInt(3, selected.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe una materia con ese nombre en la carrera seleccionada.");
                        return null;
                    }
                } catch (SQLException e) {
                    showError("Error al validar materia: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    showError("Error inesperado al validar materia: " + e.getMessage());
                    return null;
                }
                return new MateriaInput(nombre, carrera);
            }
            return null;
        });
        Optional<MateriaInput> result = dialog.showAndWait();
        result.ifPresent(input -> actualizarMateria(selected.getId(), input));
    }

    private void actualizarMateria(int idMateria, MateriaInput input) {
        String sql = "UPDATE Materia SET nombre_materia = ?, id_carrera = ? WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre_materia());
            pstmt.setInt(2, input.getCarrera().getId());
            pstmt.setInt(3, idMateria);
            pstmt.executeUpdate();
            showSuccess("Materia actualizada correctamente.");
            refreshMateriasTable();
        } catch (SQLException e) {
            showError("Error al actualizar materia: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al actualizar materia: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarMateria() {
        MateriaRow selected = materiasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione una materia para eliminar.");
            return;
        }
        // Validar que no tenga cursos asociados
        String sqlCheck = "SELECT COUNT(*) FROM Curso WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showError("No se puede eliminar la materia porque tiene cursos asociados.");
                return;
            }
        } catch (SQLException e) {
            showError("Error al validar materia: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Error inesperado al validar materia: " + e.getMessage());
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar la materia seleccionada?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarMateria(selected.getId());
        }
    }

    private void eliminarMateria(int idMateria) {
        String sql = "DELETE FROM Materia WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMateria);
            pstmt.executeUpdate();
            showSuccess("Materia eliminada correctamente.");
            refreshMateriasTable();
        } catch (SQLException e) {
            showError("Error al eliminar materia: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al eliminar materia: " + e.getMessage());
        }
    }

    private void refreshCarrerasTable() {
        if (carrerasTable == null) return;
        carrerasTable.getItems().clear();
        String sql = "SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                carrerasTable.getItems().add(new CarreraRow(
                    rs.getInt("id_carrera"),
                    rs.getString("nombre_carrera")
                ));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar carreras: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgregarCarrera() {
        Dialog<CarreraInput> dialog = new Dialog<>();
        dialog.setTitle("Agregar Carrera");
        dialog.setHeaderText("Ingrese el nombre de la nueva carrera y seleccione su facultad");
        TextField nombreField = new TextField();
        
        ComboBox<FacultadItem> facultadComboBox = new ComboBox<>();
        // Cargar facultades dinámicamente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_facultad, nombre_facultad FROM Facultad ORDER BY nombre_facultad")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                facultadComboBox.getItems().add(new FacultadItem(rs.getInt("id_facultad"), rs.getString("nombre_facultad")));
            }
        } catch (SQLException e) {
            showError("Error al cargar facultades: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar facultades: " + e.getMessage());
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre de la carrera:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(new Label("Facultad:"), 0, 1); grid.add(facultadComboBox, 1, 1);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String nombre = nombreField.getText().trim();
                FacultadItem facultad = facultadComboBox.getValue();
                
                if (nombre.isEmpty() || facultad == null) {
                    showError("Todos los campos son obligatorios.");
                    return null;
                }
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ0-9 ]+")) {
                    showError("El nombre solo puede contener letras, números y espacios.");
                    return null;
                }
                // Validar duplicado
                String sql = "SELECT COUNT(*) FROM Carrera WHERE nombre_carrera = ? AND id_facultad = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, facultad.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe una carrera con ese nombre en la facultad seleccionada.");
                        return null;
                    }
                } catch (SQLException e) {
                    showError("Error al validar carrera: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    showError("Error inesperado al validar carrera: " + e.getMessage());
                    return null;
                }
                return new CarreraInput(nombre, facultad);
            }
            return null;
        });
        Optional<CarreraInput> result = dialog.showAndWait();
        result.ifPresent(input -> agregarCarrera(input));
    }

    private void agregarCarrera(CarreraInput input) {
        String sql = "INSERT INTO Carrera (nombre_carrera, id_facultad) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre_carrera());
            pstmt.setInt(2, input.getFacultad().getId());
            pstmt.executeUpdate();
            showSuccess("Carrera agregada correctamente.");
            refreshCarrerasTable();
        } catch (SQLException e) {
            showError("Error al agregar carrera: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al agregar carrera: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditarCarrera() {
        CarreraRow selected = carrerasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione una carrera para editar.");
            return;
        }
        Dialog<CarreraInput> dialog = new Dialog<>();
        dialog.setTitle("Editar Carrera");
        dialog.setHeaderText("Editar nombre de la carrera");
        TextField nombreField = new TextField(selected.getNombre_carrera());
        
        // Añadir ComboBox para facultad en el diálogo de edición también para consistencia
        ComboBox<FacultadItem> facultadComboBox = new ComboBox<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_facultad, nombre_facultad FROM Facultad ORDER BY nombre_facultad")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                facultadComboBox.getItems().add(new FacultadItem(rs.getInt("id_facultad"), rs.getString("nombre_facultad")));
            }
        } catch (SQLException e) {
            showError("Error al cargar facultades: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar facultades: " + e.getMessage());
        }
        
        // Seleccionar la facultad actual de la carrera
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_facultad FROM Carrera WHERE id_carrera = ?")) {
            pstmt.setInt(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int idFacultadActual = rs.getInt("id_facultad");
                facultadComboBox.getItems().stream()
                               .filter(f -> f.getId() == idFacultadActual)
                               .findFirst()
                               .ifPresent(facultadComboBox::setValue);
            }
        } catch (SQLException e) {
            showError("Error al cargar facultad de la carrera: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al cargar facultad de la carrera: " + e.getMessage());
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre de la carrera:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(new Label("Facultad:"), 0, 1); grid.add(facultadComboBox, 1, 1); // Añadir al grid
        
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String nombre = nombreField.getText().trim();
                FacultadItem facultad = facultadComboBox.getValue(); // Obtener la facultad seleccionada
                
                if (nombre.isEmpty() || facultad == null) { // Validar que se seleccione una facultad
                    showError("El nombre y la facultad son obligatorios.");
                    return null;
                }
                
                if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ0-9 ]+")) {
                    showError("El nombre solo puede contener letras, números y espacios.");
                    return null;
                }
                // Validar duplicado (excluyendo la carrera actual)
                String sql = "SELECT COUNT(*) FROM Carrera WHERE nombre_carrera = ? AND id_facultad = ? AND id_carrera != ?"; // Incluir id_facultad en la validación de duplicado
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, facultad.getId()); // Usar id de la facultad seleccionada
                    pstmt.setInt(3, selected.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe una carrera con ese nombre en la facultad seleccionada."); // Mensaje de error actualizado
                        return null;
                    }
                } catch (SQLException e) {
                    showError("Error al validar carrera: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    showError("Error inesperado al validar carrera: " + e.getMessage());
                    return null;
                }
                // Pasar nombre y facultad al CarreraInput
                return new CarreraInput(nombre, facultad); 
            }
            return null;
        });
        Optional<CarreraInput> result = dialog.showAndWait();
        // Pasar id de la carrera y el CarreraInput al método de actualización
        result.ifPresent(input -> actualizarCarrera(selected.getId(), input));
    }

    private void actualizarCarrera(int idCarrera, CarreraInput input) {
        // Actualizar nombre y facultad de la carrera
        String sql = "UPDATE Carrera SET nombre_carrera = ?, id_facultad = ? WHERE id_carrera = ?"; // Incluir id_facultad en la actualización
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre_carrera());
            pstmt.setInt(2, input.getFacultad().getId()); // Obtener id de facultad del input
            pstmt.setInt(3, idCarrera);
            pstmt.executeUpdate();
            showSuccess("Carrera actualizada correctamente.");
            refreshCarrerasTable();
        } catch (SQLException e) {
            showError("Error al actualizar carrera: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al actualizar carrera: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarCarrera() {
        CarreraRow selected = carrerasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione una carrera para eliminar.");
            return;
        }
        // Validar que no tenga materias asociadas
        String sqlCheckMaterias = "SELECT COUNT(*) FROM Materia WHERE id_carrera = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheckMaterias)) {
            pstmt.setInt(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showError("No se puede eliminar la carrera porque tiene materias asociadas.");
                return;
            }
        } catch (SQLException e) {
            showError("Error al validar carrera: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Error inesperado al validar carrera: " + e.getMessage());
            return;
        }
        // Check for associated students
        String sqlCheckEst = "SELECT COUNT(*) FROM Estudiante WHERE id_carrera = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheckEst)) {
            pstmt.setInt(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showError("No se puede eliminar la carrera porque tiene estudiantes asociados.");
                return;
            }
        } catch (SQLException e) {
            showError("Error al validar carrera: " + e.getMessage());
            return;
        } catch (Exception e) {
            showError("Error inesperado al validar carrera: " + e.getMessage());
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar la carrera seleccionada?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarCarrera(selected.getId());
        }
    }

    private void eliminarCarrera(int idCarrera) {
        String sql = "DELETE FROM Carrera WHERE id_carrera = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCarrera);
            pstmt.executeUpdate();
            showSuccess("Carrera eliminada correctamente.");
            refreshCarrerasTable();
        } catch (SQLException e) {
            showError("Error al eliminar carrera: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado al eliminar carrera: " + e.getMessage());
        }
    }

    // Clase auxiliar para la tabla de carreras
    public static class CarreraRow {
        private int id;
        private String nombre_carrera;
        public CarreraRow(int id, String nombre_carrera) {
            this.id = id; this.nombre_carrera = nombre_carrera;
        }
        public int getId() { return id; }
        public String getNombre_carrera() { return nombre_carrera; }
    }

    public static class CarreraInput {
        private final String nombre_carrera;
        private final FacultadItem facultad; // Añadir campo facultad

        public CarreraInput(String nombre_carrera, FacultadItem facultad) { // Constructor con facultad
            this.nombre_carrera = nombre_carrera;
            this.facultad = facultad;
        }

        public String getNombre_carrera() { return nombre_carrera; }
        public FacultadItem getFacultad() { return facultad; } // Getter para facultad
    }

    // Clase auxiliar para representar una Facultad en ComboBox
    public static class FacultadItem {
        private int id;
        private String nombre;

        public FacultadItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() { return nombre; }
    }

    // Método auxiliar para sincronizar Calificacion tras cualquier cambio
    private void sincronizarCalificaciones() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Para cada curso, asegurar que todos los estudiantes inscritos en la materia tengan calificación
            String sqlCursos = "SELECT cu.id_curso, cu.id_materia FROM Curso cu";
            try (PreparedStatement pstmtCursos = conn.prepareStatement(sqlCursos);
                 ResultSet rsCursos = pstmtCursos.executeQuery()) {
                while (rsCursos.next()) {
                    int idCurso = rsCursos.getInt("id_curso");
                    int idMateria = rsCursos.getInt("id_materia");
                    // Buscar todos los estudiantes inscritos en esa materia
                    String sqlEsts = "SELECT e.id_estudiante FROM Estudiante e JOIN Usuario u ON e.id_usuario = u.id_usuario JOIN Calificacion c2 ON c2.id_estudiante = e.id_estudiante WHERE c2.id_curso = ? UNION SELECT e.id_estudiante FROM Estudiante e JOIN Usuario u ON e.id_usuario = u.id_usuario JOIN Calificacion c2 ON c2.id_estudiante = e.id_estudiante WHERE c2.id_curso != ? AND e.id_estudiante NOT IN (SELECT id_estudiante FROM Calificacion WHERE id_curso = ?)";
                    try (PreparedStatement pstmtEsts = conn.prepareStatement(sqlEsts)) {
                        pstmtEsts.setInt(1, idCurso);
                        pstmtEsts.setInt(2, idCurso);
                        pstmtEsts.setInt(3, idCurso);
                        ResultSet rsEsts = pstmtEsts.executeQuery();
                        while (rsEsts.next()) {
                            int idEstudiante = rsEsts.getInt("id_estudiante");
                            String sqlCal = "INSERT OR IGNORE INTO Calificacion (id_estudiante, id_curso) VALUES (?, ?)";
                            try (PreparedStatement pstmtCal = conn.prepareStatement(sqlCal)) {
                                pstmtCal.setInt(1, idEstudiante);
                                pstmtCal.setInt(2, idCurso);
                                pstmtCal.executeUpdate();
                            }
                        }
                    }
                }
            }
            // 2. Eliminar calificaciones huérfanas (sin estudiante o curso válido)
            String sqlClean = "DELETE FROM Calificacion WHERE id_estudiante NOT IN (SELECT id_estudiante FROM Estudiante) OR id_curso NOT IN (SELECT id_curso FROM Curso)";
            try (PreparedStatement pstmtClean = conn.prepareStatement(sqlClean)) {
                pstmtClean.executeUpdate();
            }
        } catch (Exception e) {
            showError("Error al sincronizar calificaciones: " + e.getMessage());
        }
    }

    private void refreshTable() {
        usuariosTable.getItems().clear();
        String sql = "SELECT u.id_usuario, u.cedula, u.nombre_usuario, u.apellido_usuario, u.rol, " +
                     "CASE WHEN u.rol = 'ESTUDIANTE' THEN c.nombre_carrera ELSE NULL END as carrera, " +
                     "CASE WHEN u.rol = 'PROFESOR' THEN GROUP_CONCAT(DISTINCT m.nombre_materia) ELSE NULL END as materias " +
                     "FROM Usuario u " +
                     "LEFT JOIN Estudiante e ON u.id_usuario = e.id_usuario " +
                     "LEFT JOIN Carrera c ON e.id_carrera = c.id_carrera " +
                     "LEFT JOIN Profesor p ON u.id_usuario = p.id_usuario " +
                     "LEFT JOIN Curso cu ON p.id_profesor = cu.id_profesor " +
                     "LEFT JOIN Materia m ON cu.id_materia = m.id_materia " +
                     "GROUP BY u.id_usuario, u.cedula, u.nombre_usuario, u.apellido_usuario, u.rol, c.nombre_carrera";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                usuariosTable.getItems().add(new UsuarioRow(
                    rs.getInt("id_usuario"),
                    rs.getString("cedula"),
                    rs.getString("nombre_usuario"),
                    rs.getString("apellido_usuario"),
                    rs.getString("rol"),
                    rs.getString("carrera"),
                    rs.getString("materias")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        UsuarioRow selected = usuariosTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione un usuario para eliminar.");
            return;
        }
        showDeleteConfirmation(selected);
    }
} 