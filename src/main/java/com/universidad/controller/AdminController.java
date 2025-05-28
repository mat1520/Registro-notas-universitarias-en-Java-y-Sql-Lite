package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Tab;

public class AdminController implements MainController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private TableView<UsuarioRow> usuariosTable;
    
    @FXML
    private TableColumn<UsuarioRow, String> cedulaColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> nombreColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> apellidoColumn;
    
    @FXML
    private TableColumn<UsuarioRow, String> rolColumn;
    
    @FXML
    private TableColumn<UsuarioRow, Void> accionesColumn;
    
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
    private TableColumn<MateriaRow, String> materiaNombreColumn;
    
    @FXML
    private TableColumn<MateriaRow, String> materiaCarreraColumn;
    
    @FXML
    private Button agregarMateriaButton;
    
    private Usuario usuario;
    
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        welcomeLabel.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        loadUsuarios();
    }
    
    @FXML
    private void initialize() {
        cedulaColumn.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));
        
        // Configurar columna de acciones
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");
            
            {
                editButton.setOnAction(event -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    showEditDialog(row);
                });
                
                deleteButton.setOnAction(event -> {
                    UsuarioRow row = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(row);
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

        // Inicializar pestaña de materias
        if (materiasTable != null) {
            materiaNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            materiaCarreraColumn.setCellValueFactory(new PropertyValueFactory<>("carrera"));
            refreshMateriasTable();
        }
    }
    
    private void loadUsuarios() {
        refreshTable();
    }
    
    private void showEditDialog(UsuarioRow row) {
        Dialog<UsuarioInput> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Editar información de " + row.getNombre() + " " + row.getApellido());
        // Crear campos del diálogo
        TextField cedulaField = new TextField(row.getCedula());
        TextField nombreField = new TextField(row.getNombre());
        TextField apellidoField = new TextField(row.getApellido());
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
        rolComboBox.setValue(row.getRol());
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));
        // ListView para materias (selección múltiple)
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(120);
        // Al cambiar carrera, cargar materias de esa carrera
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre FROM Materia WHERE id_carrera = ? ORDER BY nombre")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
                }
            }
        });
        // Si es estudiante, cargar carrera y materias actuales
        if ("ESTUDIANTE".equals(row.getRol())) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT e.id_carrera, c.nombre FROM Estudiante e " +
                     "JOIN Carrera c ON e.id_carrera = c.id_carrera " +
                     "WHERE e.id_usuario = ?")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    CarreraItem carrera = new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre"));
                    carreraComboBox.setValue(carrera);
                    carreraComboBox.getOnAction().handle(null);
                }
            } catch (Exception e) {
                showError("Error al cargar datos del estudiante: " + e.getMessage());
            }
            // Seleccionar materias actuales
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT m.id_materia FROM Materia m " +
                     "JOIN Calificacion c ON m.id_materia = c.id_materia " +
                     "WHERE c.id_estudiante = (SELECT id_estudiante FROM Estudiante WHERE id_usuario = ?)")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idMateria = rs.getInt("id_materia");
                    materiasListView.getItems().stream()
                        .filter(item -> item.id == idMateria)
                        .findFirst()
                        .ifPresent(item -> materiasListView.getSelectionModel().select(item));
                }
            } catch (Exception e) {
                showError("Error al cargar materias del estudiante: " + e.getMessage());
            }
        }
        // Si es profesor, cargar todas las materias y marcar las que califica
        if ("PROFESOR".equals(row.getRol())) {
            materiasListView.getItems().clear();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT m.id_materia, m.nombre FROM Materia m ORDER BY m.nombre")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    materiasListView.getItems().add(
                        new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                }
            } catch (Exception e) {
                showError("Error al cargar materias: " + e.getMessage());
            }
            // Seleccionar materias actuales
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT cu.id_materia FROM Curso cu " +
                     "JOIN Profesor p ON cu.id_profesor = p.id_profesor " +
                     "WHERE p.id_usuario = ?")) {
                pstmt.setInt(1, row.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idMateria = rs.getInt("id_materia");
                    materiasListView.getItems().stream()
                        .filter(item -> item.id == idMateria)
                        .findFirst()
                        .ifPresent(item -> materiasListView.getSelectionModel().select(item));
                }
            } catch (Exception e) {
                showError("Error al cargar materias del profesor: " + e.getMessage());
            }
        }
        // Layout fijo: agrego todos los campos y solo cambio visibilidad
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
        // Mostrar/ocultar según rol
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
                         "SELECT id_materia, nombre FROM Materia ORDER BY nombre")) {
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
                }
            }
        });
        // Trigger inicial
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
                                 "SELECT cu.id_profesor, p.id_usuario FROM Curso cu JOIN Profesor p ON cu.id_profesor = p.id_profesor WHERE cu.id_materia = ?")) {
                            pstmt.setInt(1, materia.getId());
                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next()) {
                                int idUsuarioAsignado = rs.getInt("id_usuario");
                                // Si es edición, permitir si es el mismo usuario
                                if (row == null || idUsuarioAsignado != row.getId()) {
                                    showError("La materia '" + materia.getNombre() + "' ya está asignada a otro profesor.");
                                    return null;
                                }
                            }
                        } catch (Exception e) {
                            showError("Error al validar materias: " + e.getMessage());
                            return null;
                        }
                    }
                }
                // Validar que se seleccionen todas las materias de la carrera
                int totalMaterias = 0;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM Materia WHERE id_carrera = ?")) {
                    pstmt.setInt(1, carrera.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) totalMaterias = rs.getInt(1);
                } catch (Exception e) {
                    showError("Error al validar materias: " + e.getMessage());
                    return null;
                }
                if (materiasSeleccionadas.size() != totalMaterias) {
                    showError("El estudiante debe estar inscrito en todas las materias de su carrera.");
                    return null;
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
        result.ifPresent(input -> updateUsuario(row, input));
    }
    
    private void updateUsuario(UsuarioRow row, UsuarioInput input) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Actualizar datos básicos del usuario
                String sql = "UPDATE Usuario SET cedula = ?, nombre = ?, apellido = ?, rol = ? WHERE id_usuario = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, input.getCedula());
                    pstmt.setString(2, input.getNombre());
                    pstmt.setString(3, input.getApellido());
                    pstmt.setString(4, input.getRol());
                    pstmt.setInt(5, row.getId());
                    pstmt.executeUpdate();
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
                            String sqlCurso = "INSERT INTO Curso (id_materia, id_profesor) VALUES (?, ?)";
                            try (PreparedStatement pstmtCurso = conn.prepareStatement(sqlCurso)) {
                                for (MateriaItem materia : input.getMateriasProfesor()) {
                                    pstmtCurso.setInt(1, materia.getId());
                                    pstmtCurso.setInt(2, idProfesor);
                                    pstmtCurso.addBatch();
                                }
                                pstmtCurso.executeBatch();
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
        alert.setHeaderText("¿Está seguro de eliminar a " + row.getNombre() + " " + row.getApellido() + "?");
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
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Gestión de Notas - Login");
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setMinWidth(600);
            stage.setMinHeight(400);
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
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(120);
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT id_materia, nombre FROM Materia WHERE id_carrera = ? ORDER BY nombre")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
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
                         "SELECT id_materia, nombre FROM Materia ORDER BY nombre")) {
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(
                            new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
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
                                showError("La materia '" + materia.getNombre() + "' ya está asignada a otro profesor.");
                                return null;
                            }
                        } catch (Exception e) {
                            showError("Error al validar materias: " + e.getMessage());
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
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, input.getCedula());
                    pstmt.setString(2, input.getNombre());
                    pstmt.setString(3, input.getApellido());
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
                                        String sqlCal = "INSERT INTO Calificacion (id_estudiante, id_materia) VALUES (?, ?)";
                                        try (PreparedStatement pstmtCal = conn.prepareStatement(sqlCal)) {
                                            pstmtCal.setInt(1, idEstudiante);
                                            pstmtCal.setInt(2, materia.getId());
                                            pstmtCal.executeUpdate();
                                        }
                                    }
                                }
                            }
                        } else if ("PROFESOR".equals(input.getRol()) && input.getMateriasProfesor() != null) {
                            String sqlProf = "INSERT INTO Profesor (id_usuario) VALUES (?)";
                            try (PreparedStatement pstmtProf = conn.prepareStatement(sqlProf, Statement.RETURN_GENERATED_KEYS)) {
                                pstmtProf.setInt(1, idUsuario);
                                pstmtProf.executeUpdate();
                                ResultSet rsProf = pstmtProf.getGeneratedKeys();
                                int idProfesor = rsProf.next() ? rsProf.getInt(1) : -1;
                                for (MateriaItem materia : input.getMateriasProfesor()) {
                                    String sqlCurso = "INSERT INTO Curso (id_materia, id_profesor) VALUES (?, ?)";
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
                conn.commit();
                showSuccess("Usuario agregado correctamente.");
                refreshTable();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            showError("Error al agregar usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditarUsuario() {
        UsuarioRow selected = usuariosTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione un usuario para editar.");
            return;
        }

        // Validar campos obligatorios
        if (cedulaField.getText().trim().isEmpty() || nombreField.getText().trim().isEmpty() || apellidoField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty() || rolCombo.getValue() == null) {
            showError("Todos los campos son obligatorios.");
            return;
        }

        // Validar formato de cédula (solo números, longitud mínima)
        if (!cedulaField.getText().trim().matches("\\d+") || cedulaField.getText().trim().length() < 5) {
            showError("La cédula debe contener solo números y tener al menos 5 dígitos.");
            return;
        }

        // Validar unicidad de cédula (excluyendo el usuario actual)
        String sqlCheck = "SELECT COUNT(*) FROM Usuario WHERE cedula = ? AND id_usuario != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setString(1, cedulaField.getText().trim());
            pstmt.setInt(2, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showError("Ya existe otro usuario con esta cédula.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error al validar la cédula: " + e.getMessage());
            return;
        }

        // Actualizar usuario
        String sql = "UPDATE Usuario SET cedula = ?, nombre = ?, apellido = ?, password = ?, rol = ? WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cedulaField.getText().trim());
            pstmt.setString(2, nombreField.getText().trim());
            pstmt.setString(3, apellidoField.getText().trim());
            pstmt.setString(4, passwordField.getText().trim());
            pstmt.setString(5, rolCombo.getValue());
            pstmt.setInt(6, selected.getId());
            pstmt.executeUpdate();

            // Actualizar carrera o materias según el rol
            if (rolCombo.getValue().equals("Estudiante") && carreraCombo.getValue() != null) {
                String sqlEstudiante = "UPDATE Estudiante SET id_carrera = ? WHERE id_usuario = ?";
                try (PreparedStatement pstmtEst = conn.prepareStatement(sqlEstudiante)) {
                    pstmtEst.setInt(1, carreraCombo.getValue().getId());
                    pstmtEst.setInt(2, selected.getId());
                    pstmtEst.executeUpdate();
                }
            } else if (rolCombo.getValue().equals("Profesor") && !materiasList.getSelectionModel().getSelectedItems().isEmpty()) {
                String sqlProfesor = "DELETE FROM ProfesorMateria WHERE id_profesor = (SELECT id_profesor FROM Profesor WHERE id_usuario = ?)";
                try (PreparedStatement pstmtProf = conn.prepareStatement(sqlProfesor)) {
                    pstmtProf.setInt(1, selected.getId());
                    pstmtProf.executeUpdate();
                }
                String sqlMaterias = "INSERT INTO ProfesorMateria (id_profesor, id_materia) SELECT id_profesor, ? FROM Profesor WHERE id_usuario = ?";
                try (PreparedStatement pstmtMat = conn.prepareStatement(sqlMaterias)) {
                    for (MateriaItem materia : materiasList.getSelectionModel().getSelectedItems()) {
                        pstmtMat.setInt(1, materia.getId());
                        pstmtMat.setInt(2, selected.getId());
                        pstmtMat.executeUpdate();
                    }
                }
            }
            showSuccess("Usuario actualizado correctamente.");
            refreshTable();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarUsuario() {
        UsuarioRow selected = usuariosTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione un usuario para eliminar.");
            return;
        }

        // Confirmación antes de eliminar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este usuario?");
        alert.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteUsuario(selected);
        }
    }

    private void refreshTable() {
        usuariosTable.getItems().clear();
        String sql = "SELECT u.id_usuario, u.cedula, u.nombre, u.apellido, u.rol, c.nombre as carrera, " +
                "GROUP_CONCAT(DISTINCT m.nombre) as materias " +
                "FROM Usuario u " +
                "LEFT JOIN Estudiante e ON u.id_usuario = e.id_usuario " +
                "LEFT JOIN Carrera c ON e.id_carrera = c.id_carrera " +
                "LEFT JOIN Profesor p ON u.id_usuario = p.id_usuario " +
                "LEFT JOIN Curso cu ON p.id_profesor = cu.id_profesor " +
                "LEFT JOIN Materia m ON cu.id_materia = m.id_materia " +
                "GROUP BY u.id_usuario";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                usuariosTable.getItems().add(new UsuarioRow(
                    rs.getInt("id_usuario"),
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
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
        private String nombre;

        public MateriaItem(int id, String nombre) {
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

    public static class UsuarioInput {
        private final String cedula, nombre, apellido, password, rol;
        private final CarreraItem carrera;
        private final java.util.List<MateriaItem> materiasEstudiante;
        private final java.util.List<MateriaItem> materiasProfesor;
        public UsuarioInput(String cedula, String nombre, String apellido, String password, String rol, CarreraItem carrera, java.util.List<MateriaItem> materiasEstudiante, java.util.List<MateriaItem> materiasProfesor) {
            this.cedula = cedula; this.nombre = nombre; this.apellido = apellido; this.password = password; this.rol = rol; this.carrera = carrera;
            this.materiasEstudiante = materiasEstudiante == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasEstudiante);
            this.materiasProfesor = materiasProfesor == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasProfesor);
        }
        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
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
        private String nombre;
        private String apellido;
        private String rol;
        private String carrera;
        private String materias;

        public UsuarioRow(int id, String cedula, String nombre, String apellido, String rol, String carrera, String materias) {
            this.id = id;
            this.cedula = cedula;
            this.nombre = nombre;
            this.apellido = apellido;
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

        public String getNombre() {
            return nombre;
        }

        public String getApellido() {
            return apellido;
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
        String sql = "SELECT m.id_materia, m.nombre, c.nombre as carrera FROM Materia m JOIN Carrera c ON m.id_carrera = c.id_carrera ORDER BY c.nombre, m.nombre";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                materiasTable.getItems().add(new MateriaRow(
                    rs.getInt("id_materia"),
                    rs.getString("nombre"),
                    rs.getString("carrera")
                ));
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgregarMateria() {
        Dialog<MateriaInput> dialog = new Dialog<>();
        dialog.setTitle("Agregar Materia");
        dialog.setHeaderText("Ingrese los datos de la nueva materia");
        TextField nombreField = new TextField();
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));
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
                String sql = "SELECT COUNT(*) FROM Materia WHERE nombre = ? AND id_carrera = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, carrera.getId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Ya existe una materia con ese nombre en la carrera seleccionada.");
                        return null;
                    }
                } catch (Exception e) {
                    showError("Error al validar materia: " + e.getMessage());
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
        String sql = "INSERT INTO Materia (nombre, id_carrera) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre());
            pstmt.setInt(2, input.getCarrera().getId());
            pstmt.executeUpdate();
            showSuccess("Materia agregada correctamente.");
            refreshMateriasTable();
        } catch (Exception e) {
            showError("Error al agregar materia: " + e.getMessage());
        }
    }

    // Clase auxiliar para la tabla de materias
    public static class MateriaRow {
        private int id;
        private String nombre;
        private String carrera;
        public MateriaRow(int id, String nombre, String carrera) {
            this.id = id; this.nombre = nombre; this.carrera = carrera;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCarrera() { return carrera; }
    }

    public static class MateriaInput {
        private final String nombre;
        private final CarreraItem carrera;
        public MateriaInput(String nombre, CarreraItem carrera) {
            this.nombre = nombre; this.carrera = carrera;
        }
        public String getNombre() { return nombre; }
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
        TextField nombreField = new TextField(selected.getNombre());
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));
        carreraComboBox.setValue(carreraComboBox.getItems().stream().filter(c -> c.getNombre().equals(selected.getCarrera())).findFirst().orElse(null));
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
                String sql = "SELECT COUNT(*) FROM Materia WHERE nombre = ? AND id_carrera = ? AND id_materia != ?";
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
                } catch (Exception e) {
                    showError("Error al validar materia: " + e.getMessage());
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
        String sql = "UPDATE Materia SET nombre = ?, id_carrera = ? WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, input.getNombre());
            pstmt.setInt(2, input.getCarrera().getId());
            pstmt.setInt(3, idMateria);
            pstmt.executeUpdate();
            showSuccess("Materia actualizada correctamente.");
            refreshMateriasTable();
        } catch (Exception e) {
            showError("Error al actualizar materia: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarMateria() {
        MateriaRow selected = materiasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Por favor seleccione una materia para eliminar.");
            return;
        }
        // Validar que no tenga cursos ni calificaciones asociadas
        String sqlCheck = "SELECT COUNT(*) FROM Curso WHERE id_materia = ? OR EXISTS (SELECT 1 FROM Calificacion WHERE id_materia = ? )";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, selected.getId());
            pstmt.setInt(2, selected.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showError("No se puede eliminar la materia porque tiene cursos o calificaciones asociadas.");
                return;
            }
        } catch (Exception e) {
            showError("Error al validar materia: " + e.getMessage());
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
        } catch (Exception e) {
            showError("Error al eliminar materia: " + e.getMessage());
        }
    }
} 