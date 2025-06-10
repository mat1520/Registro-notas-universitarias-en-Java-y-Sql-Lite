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
    
    @FXML
    private ComboBox<String> filtroRolComboBox;
    
    @FXML
    private ComboBox<CarreraItem> filtroCarreraComboBox;
    
    @FXML
    private TableColumn<UsuarioRow, String> carreraColumn;
    
    private Usuario usuario;
    
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre_usuario() + " " + usuario.getApellido_usuario());
        refreshTable();
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

        // Inicializar filtros
        filtroRolComboBox.getItems().addAll("Todos", "ADMIN", "PROFESOR", "ESTUDIANTE");
        filtroRolComboBox.setValue("Todos");
        filtroRolComboBox.setOnAction(e -> refreshTable());

        // Cargar carreras para el filtro
        cargarCarrerasParaFiltro();
        filtroCarreraComboBox.setOnAction(e -> refreshTable());

        // Configurar columna de carrera
        carreraColumn.setCellValueFactory(cellData -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String carreraNombre = obtenerCarreraUsuario(conn, cellData.getValue().getId(), cellData.getValue().getRol());
                return new javafx.beans.property.SimpleStringProperty(carreraNombre);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
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
        mostrarDialogoUsuario(null);
    }

    @FXML
    private void handleEditarUsuario() {
        UsuarioRow selected = usuariosTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione un usuario para editar.");
            return;
        }
        mostrarDialogoUsuario(selected);
    }

    private void mostrarDialogoUsuario(UsuarioRow usuarioExistente) {
        boolean modoEdicion = usuarioExistente != null;
        Dialog<UsuarioInput> dialog = new Dialog<>();
        dialog.setTitle(modoEdicion ? "Editar Usuario" : "Agregar Usuario");
        dialog.setHeaderText(modoEdicion ? 
            "Editar información de " + usuarioExistente.getNombre_usuario() + " " + usuarioExistente.getApellido_usuario() :
            "Ingrese los datos del nuevo usuario");

        // Campos del formulario
        TextField cedulaField = new TextField();
        TextField nombreField = new TextField();
        TextField apellidoField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button generarPassBtn = new Button("Generar contraseña segura");
        ComboBox<String> rolComboBox = new ComboBox<>();
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        ListView<MateriaItem> materiasListView = new ListView<>();

        // Configurar campos si estamos en modo edición
        if (modoEdicion) {
            cedulaField.setText(usuarioExistente.getCedula());
            nombreField.setText(usuarioExistente.getNombre_usuario());
            apellidoField.setText(usuarioExistente.getApellido_usuario());
            passwordField.setPromptText("Dejar en blanco para mantener la contraseña actual");
        }

        // Configurar botón de generación de contraseña
        generarPassBtn.setOnAction(ev -> {
            String pass = generarPasswordSegura();
            passwordField.setText(pass);
            ClipboardContent content = new ClipboardContent();
            content.putString(pass);
            Clipboard.getSystemClipboard().setContent(content);
            showInfo("Contraseña generada y copiada al portapapeles.");
        });

        // Configurar ComboBox de roles
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        if (modoEdicion) {
            rolComboBox.setValue(usuarioExistente.getRol());
        } else {
            rolComboBox.setValue("ESTUDIANTE");
        }

        // Cargar carreras
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
            return;
        }

        // Configurar ListView de materias
        materiasListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (modoEdicion) {
            // Cargar materias asignadas al usuario
            try (Connection conn = DatabaseConnection.getConnection()) {
                if ("ESTUDIANTE".equals(usuarioExistente.getRol())) {
                    String sql = "SELECT m.id_materia, m.nombre_materia FROM Materia m " +
                               "JOIN Curso cu ON m.id_materia = cu.id_materia " +
                               "JOIN Calificacion c ON cu.id_curso = c.id_curso " +
                               "JOIN Estudiante e ON c.id_estudiante = e.id_estudiante " +
                               "WHERE e.id_usuario = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, usuarioExistente.getId());
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                        }
                    }
                } else if ("PROFESOR".equals(usuarioExistente.getRol())) {
                    String sql = "SELECT m.id_materia, m.nombre_materia FROM Materia m " +
                               "JOIN Curso c ON m.id_materia = c.id_materia " +
                               "WHERE c.id_profesor = (SELECT id_profesor FROM Profesor WHERE id_usuario = ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, usuarioExistente.getId());
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                        }
                    }
                }
            } catch (SQLException e) {
                showError("Error al cargar materias asignadas: " + e.getMessage());
                return;
            }
        }

        // Configurar GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Cédula:"), 0, 0);
        grid.add(cedulaField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Apellido:"), 0, 2);
        grid.add(apellidoField, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(generarPassBtn, 2, 3);
        grid.add(new Label("Rol:"), 0, 4);
        grid.add(rolComboBox, 1, 4);
        grid.add(new Label("Carrera:"), 0, 5);
        grid.add(carreraComboBox, 1, 5);
        grid.add(new Label("Materias:"), 0, 6);
        grid.add(materiasListView, 1, 6);

        // Configurar visibilidad de campos según rol
        rolComboBox.setOnAction(e -> {
            String rol = rolComboBox.getValue();
            carreraComboBox.setVisible("ESTUDIANTE".equals(rol) || "PROFESOR".equals(rol));
            materiasListView.setVisible("ESTUDIANTE".equals(rol) || "PROFESOR".equals(rol));
            // Al cambiar de rol, actualizar materias según la carrera seleccionada
            CarreraItem carrera = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (("ESTUDIANTE".equals(rol) || "PROFESOR".equals(rol)) && carrera != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id_materia, nombre_materia FROM Materia WHERE id_carrera = ? ORDER BY nombre_materia")) {
                    pstmt.setInt(1, carrera.getId());
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException ex) {
                    showError("Error al cargar materias: " + ex.getMessage());
                }
            }
        });

        // Evento para actualizar materias al cambiar la carrera si el rol es estudiante o profesor
        carreraComboBox.setOnAction(e -> {
            String rol = rolComboBox.getValue();
            materiasListView.getItems().clear();
            CarreraItem carrera = carreraComboBox.getValue();
            if (("ESTUDIANTE".equals(rol) || "PROFESOR".equals(rol)) && carrera != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id_materia, nombre_materia FROM Materia WHERE id_carrera = ? ORDER BY nombre_materia")) {
                    pstmt.setInt(1, carrera.getId());
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre_materia")));
                    }
                } catch (SQLException ex) {
                    showError("Error al cargar materias: " + ex.getMessage());
                }
            }
        });

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Configurar conversor de resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cedula = cedulaField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String password = passwordField.getText();
                String rol = rolComboBox.getValue();
                CarreraItem carrera = carreraComboBox.getValue();
                List<MateriaItem> materiasSeleccionadas = new ArrayList<>(materiasListView.getSelectionModel().getSelectedItems());

                // Validaciones
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
                    showError("Todos los campos son obligatorios.");
                    return null;
                }

                if (!modoEdicion && password.isEmpty()) {
                    showError("La contraseña es obligatoria para nuevos usuarios.");
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
                                if (modoEdicion && idUsuarioAsignado == usuarioExistente.getId()) {
                                    continue; // Es el mismo profesor, permitir
                                }
                                showError("La materia '" + materia.getNombre_materia() + "' ya está asignada a otro profesor.");
                                return null;
                            }
                        } catch (SQLException e) {
                            showError("Error al validar materias: " + e.getMessage());
                            return null;
                        }
                    }
                }

                if (modoEdicion) {
                    // Validar unicidad de cédula excluyendo el usuario actual
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT COUNT(*) FROM Usuario WHERE cedula = ? AND id_usuario != ?")) {
                        pstmt.setString(1, cedula);
                        pstmt.setInt(2, usuarioExistente.getId());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            showError("Ya existe otro usuario con esta cédula.");
                            return null;
                        }
                    } catch (SQLException e) {
                        showError("Error al validar la cédula: " + e.getMessage());
                        return null;
                    }
                } else {
                    // Validar unicidad de cédula para nuevo usuario
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT COUNT(*) FROM Usuario WHERE cedula = ?")) {
                        pstmt.setString(1, cedula);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            showError("Ya existe un usuario con esta cédula.");
                            return null;
                        }
                    } catch (SQLException e) {
                        showError("Error al validar la cédula: " + e.getMessage());
                        return null;
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
        result.ifPresent(input -> {
            if (modoEdicion) {
                updateUsuario(usuarioExistente, input);
            } else {
                agregarUsuario(input);
            }
        });
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
            showInfo("Usuario agregado correctamente.");
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
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            // Si es estudiante, eliminar subnotas, calificaciones y registro de estudiante
            if ("ESTUDIANTE".equals(row.getRol())) {
                // Obtener id_estudiante
                int idEstudiante = -1;
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_estudiante FROM Estudiante WHERE id_usuario = ?")) {
                    pstmt.setInt(1, row.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        idEstudiante = rs.getInt(1);
                    }
                }
                if (idEstudiante != -1) {
                    // Eliminar subnotas
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Subnota WHERE id_calificacion IN (SELECT id_calificacion FROM Calificacion WHERE id_estudiante = ? )")) {
                        pstmt.setInt(1, idEstudiante);
                        pstmt.executeUpdate();
                    }
                    // Eliminar calificaciones
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Calificacion WHERE id_estudiante = ?")) {
                        pstmt.setInt(1, idEstudiante);
                        pstmt.executeUpdate();
                    }
                    // Eliminar estudiante
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Estudiante WHERE id_estudiante = ?")) {
                        pstmt.setInt(1, idEstudiante);
                        pstmt.executeUpdate();
                    }
                }
            }
            // Eliminar usuario (siempre)
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Usuario WHERE id_usuario = ?")) {
                pstmt.setInt(1, row.getId());
                pstmt.executeUpdate();
            }
            conn.commit();
            // Actualizar la tabla
            usuariosTable.getItems().remove(row);
        } catch (Exception e) {
            showError("Error al eliminar usuario y datos relacionados: " + e.getMessage());
        }
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
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : 
                      type == Alert.AlertType.INFORMATION ? "Información" : 
                      type == Alert.AlertType.CONFIRMATION ? "Confirmar" : "");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        showAlert(message, Alert.AlertType.ERROR);
    }
    
    private void showInfo(String message) {
        showAlert(message, Alert.AlertType.INFORMATION);
    }
    
    private void showSuccess(String message) {
        showAlert(message, Alert.AlertType.INFORMATION);
    }
    
    public static class UsuarioRow {
        private int id;
        private String cedula;
        private String nombre_usuario;
        private String apellido_usuario;
        private String rol;

        public UsuarioRow(int id, String cedula, String nombre_usuario, String apellido_usuario, String rol) {
            this.id = id;
            this.cedula = cedula;
            this.nombre_usuario = nombre_usuario;
            this.apellido_usuario = apellido_usuario;
            this.rol = rol;
        }

        public int getId() { return id; }
        public String getCedula() { return cedula; }
        public String getNombre_usuario() { return nombre_usuario; }
        public String getApellido_usuario() { return apellido_usuario; }
        public String getRol() { return rol; }
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
                if (!validarNombre(nombre, "materia")) {
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
                if (!validarNombre(nombre, "materia")) {
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

        // Verificar si la materia está asignada a algún profesor
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT p.nombre_usuario || ' ' || p.apellido_usuario as nombre_profesor " +
                 "FROM Curso c " +
                 "JOIN Profesor pr ON c.id_profesor = pr.id_profesor " +
                 "JOIN Usuario p ON pr.id_usuario = p.id_usuario " +
                 "WHERE c.id_materia = ? " +
                 "LIMIT 1")) {
            
            pstmt.setInt(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String nombreProfesor = rs.getString("nombre_profesor");
                showError("No se puede eliminar la materia porque está asignada al profesor: " + nombreProfesor);
                return;
            }
        } catch (SQLException e) {
            showError("Error al verificar asignaciones de la materia: " + e.getMessage());
            return;
        }

        // Si no está asignada, mostrar confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar la materia " + selected.getNombre_materia() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteMateria(selected);
        }
    }

    private void deleteMateria(MateriaRow materia) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Eliminar la materia
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Materia WHERE id_materia = ?")) {
                    pstmt.setInt(1, materia.getId());
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                materiasTable.getItems().remove(materia);
                showSuccess("Materia eliminada correctamente.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showError("Error al eliminar la materia: " + e.getMessage());
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
                if (!validarNombre(nombre, "carrera")) {
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
                
                if (!validarNombre(nombre, "carrera")) {
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

        // Verificar si hay materias o estudiantes en la carrera
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verificar materias
            try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM Materia WHERE id_carrera = ?")) {
                pstmt.setInt(1, selected.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showError("No se puede eliminar la carrera porque tiene materias asignadas. Elimine primero las materias.");
                    return;
                }
            }

            // Verificar estudiantes
            try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM Estudiante WHERE id_carrera = ?")) {
                pstmt.setInt(1, selected.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showError("No se puede eliminar la carrera porque tiene estudiantes inscritos.");
                    return;
                }
            }
        } catch (SQLException e) {
            showError("Error al verificar dependencias de la carrera: " + e.getMessage());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar la carrera " + selected.getNombre_carrera() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarCarrera(selected.getId());
        }
    }

    private void eliminarCarrera(int idCarrera) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Carrera WHERE id_carrera = ?")) {
                    pstmt.setInt(1, idCarrera);
                    pstmt.executeUpdate();
                }
                conn.commit();
                refreshCarrerasTable();
                showSuccess("Carrera eliminada exitosamente.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showError("Error al eliminar carrera: " + e.getMessage());
        }
    }

    // Clase auxiliar para la tabla de carreras
    public static class CarreraRow {
        private int id;
        private String nombre_carrera;
        public CarreraRow(int id, String nombre_carrera) {
            this.id = id;
            this.nombre_carrera = nombre_carrera;
        }
        public int getId() { return id; }
        public String getNombre_carrera() { return nombre_carrera; }
    }

    public static class CarreraInput {
        private final String nombre_carrera;
        private final FacultadItem facultad;
        
        public CarreraInput(String nombre_carrera, FacultadItem facultad) {
            this.nombre_carrera = nombre_carrera;
            this.facultad = facultad;
        }
        
        public String getNombre_carrera() { return nombre_carrera; }
        public FacultadItem getFacultad() { return facultad; }
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

    private void cargarCarrerasParaFiltro() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre_carrera FROM Carrera ORDER BY nombre_carrera")) {
            ResultSet rs = pstmt.executeQuery();
            filtroCarreraComboBox.getItems().clear();
            filtroCarreraComboBox.getItems().add(new CarreraItem(0, "Todas"));
            while (rs.next()) {
                filtroCarreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre_carrera")));
            }
            filtroCarreraComboBox.setValue(filtroCarreraComboBox.getItems().get(0));
        } catch (SQLException e) {
            showError("Error al cargar carreras: " + e.getMessage());
        }
    }

    private String obtenerCarreraUsuario(Connection conn, int idUsuario, String rol) throws SQLException {
        if ("ESTUDIANTE".equals(rol)) {
            String sql = "SELECT c.nombre_carrera FROM Carrera c " +
                        "JOIN Estudiante e ON c.id_carrera = e.id_carrera " +
                        "WHERE e.id_usuario = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idUsuario);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("nombre_carrera");
                }
            }
        } else if ("PROFESOR".equals(rol)) {
            String sql = "SELECT DISTINCT c.nombre_carrera FROM Carrera c " +
                        "JOIN Materia m ON c.id_carrera = m.id_carrera " +
                        "JOIN Curso cu ON m.id_materia = cu.id_materia " +
                        "JOIN Profesor p ON cu.id_profesor = p.id_profesor " +
                        "WHERE p.id_usuario = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idUsuario);
                ResultSet rs = pstmt.executeQuery();
                StringBuilder carreras = new StringBuilder();
                while (rs.next()) {
                    if (carreras.length() > 0) carreras.append(", ");
                    carreras.append(rs.getString("nombre_carrera"));
                }
                return carreras.toString();
            }
        }
        return "";
    }

    @FXML
    private void handleLimpiarFiltros() {
        filtroRolComboBox.setValue("Todos");
        filtroCarreraComboBox.setValue(filtroCarreraComboBox.getItems().get(0));
        refreshTable();
    }

    private void refreshTable() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT u.id_usuario, u.cedula, u.nombre_usuario, u.apellido_usuario, u.rol " +
                "FROM Usuario u "
            );

            List<Object> params = new ArrayList<>();
            boolean whereAdded = false;

            // Filtro por rol
            String rolSeleccionado = filtroRolComboBox.getValue();
            if (!"Todos".equals(rolSeleccionado)) {
                sql.append(" WHERE u.rol = ?");
                params.add(rolSeleccionado);
                whereAdded = true;
            }

            // Filtro por carrera
            CarreraItem carreraSeleccionada = filtroCarreraComboBox.getValue();
            if (carreraSeleccionada != null && carreraSeleccionada.getId() != 0) {
                sql.append(whereAdded ? " AND" : " WHERE");
                sql.append(" (EXISTS (SELECT 1 FROM Estudiante e WHERE e.id_usuario = u.id_usuario AND e.id_carrera = ?) OR " +
                          "EXISTS (SELECT 1 FROM Profesor p " +
                          "JOIN Curso cu ON p.id_profesor = cu.id_profesor " +
                          "JOIN Materia m ON cu.id_materia = m.id_materia " +
                          "WHERE p.id_usuario = u.id_usuario AND m.id_carrera = ?))");
                params.add(carreraSeleccionada.getId());
                params.add(carreraSeleccionada.getId());
            }

            sql.append(" ORDER BY u.rol, u.nombre_usuario");

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                usuariosTable.getItems().clear();
                while (rs.next()) {
                    usuariosTable.getItems().add(new UsuarioRow(
                        rs.getInt("id_usuario"),
                        rs.getString("cedula"),
                        rs.getString("nombre_usuario"),
                        rs.getString("apellido_usuario"),
                        rs.getString("rol")
                    ));
                }
            }
        } catch (SQLException e) {
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

    private boolean validarNombre(String nombre, String tipo) {
        if (nombre.isEmpty()) {
            showError("El nombre es obligatorio.");
            return false;
        }
        if (!nombre.matches("[A-Za-záéíóúÁÉÍÓÚñÑ0-9 ]+")) {
            showError("El nombre solo puede contener letras, números y espacios.");
            return false;
        }
        return true;
    }
} 