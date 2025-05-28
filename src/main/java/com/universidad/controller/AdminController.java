package com.universidad.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    }
    
    private void loadUsuarios() {
        String sql = "SELECT * FROM Usuario ORDER BY nombre, apellido";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            usuariosTable.getItems().clear();
            while (rs.next()) {
                UsuarioRow row = new UsuarioRow(
                    rs.getInt("id_usuario"),
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("rol"),
                    rs.getString("password")
                );
                usuariosTable.getItems().add(row);
            }
        } catch (Exception e) {
            showError("Error al cargar usuarios: " + e.getMessage());
        }
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
        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        rolComboBox.setValue(row.getRol());

        // Campos dinámicos
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        Label carreraLabel = new Label("Carrera:");
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(100);
        Label materiasProfesorLabel = new Label("Materias a calificar:");
        ListView<MateriaItem> materiasProfesorListView = new ListView<>();
        materiasProfesorListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasProfesorListView.setPrefHeight(100);

        // Poblar carreras
        carreraComboBox.getItems().clear();
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));

        // Cargar datos actuales si es estudiante
        if ("ESTUDIANTE".equals(row.getRol())) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT e.id_carrera, c.nombre FROM Estudiante e " +
                     "JOIN Carrera c ON e.id_carrera = c.id_carrera " +
                     "WHERE e.id_usuario = ?")) {
                pstmt.setInt(1, row.getIdUsuario());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    carreraComboBox.setValue(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre")));
                }
            } catch (Exception e) {
                showError("Error al cargar datos del estudiante: " + e.getMessage());
            }
        }

        // Poblar materias según carrera seleccionada
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

        // Poblar materias para profesor
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT m.id_materia, m.nombre, c.nombre as carrera_nombre " +
                 "FROM Materia m " +
                 "JOIN Carrera c ON m.id_carrera = c.id_carrera " +
                 "ORDER BY c.nombre, m.nombre")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String carreraNombre = rs.getString("carrera_nombre");
                String materiaNombre = rs.getString("nombre");
                materiasProfesorListView.getItems().add(
                    new MateriaItem(rs.getInt("id_materia"), carreraNombre + " - " + materiaNombre));
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
        }

        // Si es profesor, cargar materias actuales
        if ("PROFESOR".equals(row.getRol())) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT c.id_materia FROM Curso c " +
                     "JOIN Profesor p ON c.id_profesor = p.id_profesor " +
                     "WHERE p.id_usuario = ?")) {
                pstmt.setInt(1, row.getIdUsuario());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idMateria = rs.getInt("id_materia");
                    materiasProfesorListView.getItems().stream()
                        .filter(item -> item.id == idMateria)
                        .findFirst()
                        .ifPresent(item -> materiasProfesorListView.getSelectionModel().select(item));
                }
            } catch (Exception e) {
                showError("Error al cargar materias del profesor: " + e.getMessage());
            }
        }

        // Cambiar campos según rol
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabel.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasProfesorLabel.setVisible(esProfesor);
            materiasProfesorListView.setVisible(esProfesor);
        });

        // Layout fijo: agrego todos los campos y solo cambio visibilidad
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int layoutRow = 0;
        grid.add(new Label("Cédula:"), 0, layoutRow); grid.add(cedulaField, 1, layoutRow++);
        grid.add(new Label("Nombre:"), 0, layoutRow); grid.add(nombreField, 1, layoutRow++);
        grid.add(new Label("Apellido:"), 0, layoutRow); grid.add(apellidoField, 1, layoutRow++);
        grid.add(new Label("Contraseña:"), 0, layoutRow); grid.add(passwordField, 1, layoutRow++);
        grid.add(new Label("Rol:"), 0, layoutRow); grid.add(rolComboBox, 1, layoutRow++);
        // Paneles para cada rol (siempre agregados)
        Label carreraLabelEdit = new Label("Carrera:");
        grid.add(carreraLabelEdit, 0, layoutRow); grid.add(carreraComboBox, 1, layoutRow++);
        Label materiasLabelEdit = new Label("Materias a cursar:");
        grid.add(materiasLabelEdit, 0, layoutRow); grid.add(materiasListView, 1, layoutRow++);
        Label materiasProfesorLabelEdit = new Label("Materias a calificar:");
        grid.add(materiasProfesorLabelEdit, 0, layoutRow); grid.add(materiasProfesorListView, 1, layoutRow++);
        // Listeners para mostrar/ocultar según rol
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabelEdit.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasLabelEdit.setVisible(esEstudiante);
            materiasListView.setVisible(esEstudiante);
            materiasProfesorLabelEdit.setVisible(esProfesor);
            materiasProfesorListView.setVisible(esProfesor);
        });
        // Trigger inicial
        rolComboBox.getOnAction().handle(null);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Validación y guardado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cedula = cedulaField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String password = passwordField.getText();
                String rol = rolComboBox.getValue();
                CarreraItem carrera = carreraComboBox.getValue();
                var materiasEst = materiasListView.getSelectionModel().getSelectedItems();
                var materiasProf = materiasProfesorListView.getSelectionModel().getSelectedItems();
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || rol == null) {
                    showError("Todos los campos obligatorios deben estar llenos.");
                    return null;
                }
                if (!validarCedulaEcuatoriana(cedula)) {
                    showError("Cédula ecuatoriana no válida.");
                    return null;
                }
                if ("ESTUDIANTE".equals(rol) && carrera == null) {
                    showError("Debe seleccionar una carrera para el estudiante.");
                    return null;
                }
                return new UsuarioInput(
                    cedula, nombre, apellido, 
                    password.isEmpty() ? row.getPassword() : password,
                    rol, carrera,
                    new java.util.ArrayList<>(materiasEst),
                    new java.util.ArrayList<>(materiasProf)
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
                    pstmt.setInt(5, row.getIdUsuario());
                    pstmt.executeUpdate();
                }

                // Si es profesor, actualizar materias a calificar
                if ("PROFESOR".equals(input.getRol())) {
                    // Obtener id_profesor
                    int idProfesor = -1;
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_profesor FROM Profesor WHERE id_usuario = ?")) {
                        pstmt.setInt(1, row.getIdUsuario());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) idProfesor = rs.getInt(1);
                    }

                    if (idProfesor != -1) {
                        // Eliminar cursos actuales
                        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Curso WHERE id_profesor = ?")) {
                            pstmt.setInt(1, idProfesor);
                            pstmt.executeUpdate();
                        }

                        // Insertar nuevos cursos
                        if (input.getMateriasProfesor() != null && !input.getMateriasProfesor().isEmpty()) {
                            String sqlCurso = "INSERT INTO Curso (id_materia, id_profesor) VALUES (?, ?)";
                            try (PreparedStatement pstmtCurso = conn.prepareStatement(sqlCurso)) {
                                for (MateriaItem materia : input.getMateriasProfesor()) {
                                    pstmtCurso.setInt(1, materia.id);
                                    pstmtCurso.setInt(2, idProfesor);
                                    pstmtCurso.addBatch();
                                }
                                pstmtCurso.executeBatch();
                            }
                        }
                    }
                }

                // Actualizar la tabla
                row.setCedula(input.getCedula());
                row.setNombre(input.getNombre());
                row.setApellido(input.getApellido());
                row.setRol(input.getRol());
                usuariosTable.refresh();

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
            
            pstmt.setInt(1, row.getIdUsuario());
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

        // Campos base
        TextField cedulaField = new TextField();
        TextField nombreField = new TextField();
        TextField apellidoField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        rolComboBox.setValue("ESTUDIANTE");

        // Campos para estudiante
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        carreraComboBox.getItems().add(new CarreraItem(1, "ING SISTEMAS"));
        carreraComboBox.getItems().add(new CarreraItem(2, "ING MECATRONICA"));
        carreraComboBox.setValue(carreraComboBox.getItems().get(0));
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(100);
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id_materia, nombre FROM Materia WHERE id_carrera = ? ORDER BY nombre")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
                }
            }
        });
        carreraComboBox.getOnAction().handle(null);

        // Campos para profesor
        ListView<MateriaItem> materiasProfesorListView = new ListView<>();
        materiasProfesorListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasProfesorListView.setPrefHeight(100);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT m.id_materia, m.nombre, c.nombre as carrera_nombre " +
                 "FROM Materia m " +
                 "JOIN Carrera c ON m.id_carrera = c.id_carrera " +
                 "ORDER BY c.nombre, m.nombre");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String carreraNombre = rs.getString("carrera_nombre");
                String materiaNombre = rs.getString("nombre");
                materiasProfesorListView.getItems().add(
                    new MateriaItem(rs.getInt("id_materia"), carreraNombre + " - " + materiaNombre));
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
        }

        // Layout fijo: agrego todos los campos y solo cambio visibilidad
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int layoutRow = 0;
        grid.add(new Label("Cédula:"), 0, layoutRow); grid.add(cedulaField, 1, layoutRow++);
        grid.add(new Label("Nombre:"), 0, layoutRow); grid.add(nombreField, 1, layoutRow++);
        grid.add(new Label("Apellido:"), 0, layoutRow); grid.add(apellidoField, 1, layoutRow++);
        grid.add(new Label("Contraseña:"), 0, layoutRow); grid.add(passwordField, 1, layoutRow++);
        grid.add(new Label("Rol:"), 0, layoutRow); grid.add(rolComboBox, 1, layoutRow++);
        // Paneles para cada rol (siempre agregados)
        Label carreraLabelEdit = new Label("Carrera:");
        grid.add(carreraLabelEdit, 0, layoutRow); grid.add(carreraComboBox, 1, layoutRow++);
        Label materiasLabelEdit = new Label("Materias a cursar:");
        grid.add(materiasLabelEdit, 0, layoutRow); grid.add(materiasListView, 1, layoutRow++);
        Label materiasProfesorLabelEdit = new Label("Materias a calificar:");
        grid.add(materiasProfesorLabelEdit, 0, layoutRow); grid.add(materiasProfesorListView, 1, layoutRow++);
        // Listeners para mostrar/ocultar según rol
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabelEdit.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasLabelEdit.setVisible(esEstudiante);
            materiasListView.setVisible(esEstudiante);
            materiasProfesorLabelEdit.setVisible(esProfesor);
            materiasProfesorListView.setVisible(esProfesor);
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
                var materiasEst = materiasListView.getSelectionModel().getSelectedItems();
                var materiasProf = materiasProfesorListView.getSelectionModel().getSelectedItems();
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || password.isEmpty() || rol == null) {
                    showError("Todos los campos obligatorios deben estar llenos.");
                    return null;
                }
                if (!validarCedulaEcuatoriana(cedula)) {
                    showError("Cédula ecuatoriana no válida.");
                    return null;
                }
                return new UsuarioInput(cedula, nombre, apellido, password, rol, carrera, new java.util.ArrayList<>(materiasEst), new java.util.ArrayList<>(materiasProf));
            }
            return null;
        });
        Optional<UsuarioInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                // Insertar en Usuario
                String sqlUsuario = "INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, input.getCedula());
                    pstmt.setString(2, input.getNombre());
                    pstmt.setString(3, input.getApellido());
                    pstmt.setString(4, input.getPassword());
                    pstmt.setString(5, input.getRol());
                    pstmt.executeUpdate();
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        int idUsuario = rs.getInt(1);
                        if ("ESTUDIANTE".equals(input.getRol())) {
                            // Insertar en Estudiante
                            String sqlEst = "INSERT INTO Estudiante (id_usuario, id_carrera) VALUES (?, ?)";
                            int idCarrera = input.getCarrera().id;
                            int idEstudiante = -1;
                            try (PreparedStatement pstmtEst = conn.prepareStatement(sqlEst, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                pstmtEst.setInt(1, idUsuario);
                                pstmtEst.setInt(2, idCarrera);
                                pstmtEst.executeUpdate();
                                ResultSet rsEst = pstmtEst.getGeneratedKeys();
                                if (rsEst.next()) {
                                    idEstudiante = rsEst.getInt(1);
                                }
                            }
                            // Inscribir automáticamente en todas las materias seleccionadas
                            if (idEstudiante != -1 && input.getMateriasEstudiante() != null) {
                                for (MateriaItem materia : input.getMateriasEstudiante()) {
                                    // Buscar cursos existentes para esa materia
                                    try (PreparedStatement pstmtCurso = conn.prepareStatement("SELECT id_curso FROM Curso WHERE id_materia = ? LIMIT 1")) {
                                        pstmtCurso.setInt(1, materia.id);
                                        ResultSet rsCurso = pstmtCurso.executeQuery();
                                        if (rsCurso.next()) {
                                            int idCurso = rsCurso.getInt(1);
                                            // Inscribir
                                            try (PreparedStatement pstmtIns = conn.prepareStatement("INSERT OR IGNORE INTO Calificacion (id_estudiante, id_curso) VALUES (?, ?)")) {
                                                pstmtIns.setInt(1, idEstudiante);
                                                pstmtIns.setInt(2, idCurso);
                                                pstmtIns.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        } else if ("PROFESOR".equals(input.getRol())) {
                            // Insertar en Profesor
                            String sqlProf = "INSERT INTO Profesor (id_usuario) VALUES (?)";
                            int idProfesor = -1;
                            try (PreparedStatement pstmtProf = conn.prepareStatement(sqlProf, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                pstmtProf.setInt(1, idUsuario);
                                pstmtProf.executeUpdate();
                                ResultSet rsProf = pstmtProf.getGeneratedKeys();
                                if (rsProf.next()) {
                                    idProfesor = rsProf.getInt(1);
                                }
                            }
                            // Insertar cursos para el profesor
                            if (idProfesor != -1 && input.getMateriasProfesor() != null) {
                                for (MateriaItem materia : input.getMateriasProfesor()) {
                                    try (PreparedStatement pstmtCurso = conn.prepareStatement("INSERT INTO Curso (id_materia, id_profesor) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                                        pstmtCurso.setInt(1, materia.id);
                                        pstmtCurso.setInt(2, idProfesor);
                                        pstmtCurso.executeUpdate();
                                        ResultSet rsCurso = pstmtCurso.getGeneratedKeys();
                                        if (rsCurso.next()) {
                                            int idCurso = rsCurso.getInt(1);
                                            // Inscribir automáticamente al menos un estudiante de la carrera de la materia
                                            try (PreparedStatement pstmtEst = conn.prepareStatement("SELECT e.id_estudiante FROM Estudiante e JOIN Materia m ON e.id_carrera = m.id_carrera WHERE m.id_materia = ? LIMIT 1")) {
                                                pstmtEst.setInt(1, materia.id);
                                                ResultSet rsEst = pstmtEst.executeQuery();
                                                if (rsEst.next()) {
                                                    int idEstudiante = rsEst.getInt(1);
                                                    try (PreparedStatement pstmtCal = conn.prepareStatement("INSERT OR IGNORE INTO Calificacion (id_estudiante, id_curso) VALUES (?, ?)")) {
                                                        pstmtCal.setInt(1, idEstudiante);
                                                        pstmtCal.setInt(2, idCurso);
                                                        pstmtCal.executeUpdate();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        conn.commit();
                        loadUsuarios();
                    }
                } catch (Exception e) {
                    conn.rollback();
                    showError("Error al guardar usuario: " + e.getMessage());
                }
            } catch (Exception e) {
                showError("Error al guardar usuario: " + e.getMessage());
            }
        });
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
    private static class CarreraItem {
        int id;
        String nombre;
        CarreraItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
    private static class MateriaItem {
        int id;
        String nombre;
        MateriaItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
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
    
    public static class UsuarioRow {
        private final int idUsuario;
        private String cedula;
        private String nombre;
        private String apellido;
        private String rol;
        private String password;
        
        public UsuarioRow(int idUsuario, String cedula, String nombre, String apellido, String rol, String password) {
            this.idUsuario = idUsuario;
            this.cedula = cedula;
            this.nombre = nombre;
            this.apellido = apellido;
            this.rol = rol;
            this.password = password;
        }
        
        public int getIdUsuario() { return idUsuario; }
        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getRol() { return rol; }
        public String getPassword() { return password; }
        
        public void setCedula(String cedula) { this.cedula = cedula; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public void setRol(String rol) { this.rol = rol; }
        public void setPassword(String password) { this.password = password; }
    }
} 