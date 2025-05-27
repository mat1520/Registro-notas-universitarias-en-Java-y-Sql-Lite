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
                    rs.getString("rol")
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
        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.getItems().addAll("ESTUDIANTE", "PROFESOR", "ADMIN");
        rolComboBox.setValue(row.getRol());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Cédula:"), 0, 0);
        grid.add(cedulaField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Apellido:"), 0, 2);
        grid.add(apellidoField, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(rolComboBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Agregar botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Convertir resultado a UsuarioInput
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new UsuarioInput(
                    cedulaField.getText(),
                    nombreField.getText(),
                    apellidoField.getText(),
                    "", // password vacío para edición
                    rolComboBox.getValue(),
                    null, // carrera
                    null, // materias estudiante
                    null, // materias profesor
                    "", // título
                    ""  // especialidad
                );
            }
            return null;
        });
        
        Optional<UsuarioInput> result = dialog.showAndWait();
        result.ifPresent(input -> updateUsuario(row, input));
    }
    
    private void updateUsuario(UsuarioRow row, UsuarioInput input) {
        String sql = "UPDATE Usuario SET cedula = ?, nombre = ?, apellido = ?, rol = ? WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, input.getCedula());
            pstmt.setString(2, input.getNombre());
            pstmt.setString(3, input.getApellido());
            pstmt.setString(4, input.getRol());
            pstmt.setInt(5, row.getIdUsuario());
            
            pstmt.executeUpdate();
            
            // Actualizar la tabla
            row.setCedula(input.getCedula());
            row.setNombre(input.getNombre());
            row.setApellido(input.getApellido());
            row.setRol(input.getRol());
            usuariosTable.refresh();
            
        } catch (Exception e) {
            showError("Error al actualizar usuario: " + e.getMessage());
        }
    }
    
    private void showDeleteConfirmation(UsuarioRow row) {
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

        // Campos dinámicos
        ComboBox<CarreraItem> carreraComboBox = new ComboBox<>();
        carreraComboBox.setDisable(false);
        Label carreraLabel = new Label("Carrera:");
        carreraLabel.setVisible(true);
        carreraComboBox.setVisible(true);

        // Materias para estudiante
        ListView<MateriaItem> materiasListView = new ListView<>();
        materiasListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasListView.setPrefHeight(100);
        Label materiasLabel = new Label("Materias a cursar:");
        materiasLabel.setVisible(true);
        materiasListView.setVisible(true);

        // Materias para profesor
        ListView<MateriaItem> materiasProfesorListView = new ListView<>();
        materiasProfesorListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        materiasProfesorListView.setPrefHeight(100);
        Label materiasProfesorLabel = new Label("Materias a calificar:");
        materiasProfesorLabel.setVisible(false);
        materiasProfesorListView.setVisible(false);

        // Título y especialidad para profesor
        TextField tituloField = new TextField();
        TextField especialidadField = new TextField();
        Label tituloLabel = new Label("Título:");
        Label especialidadLabel = new Label("Especialidad:");
        tituloLabel.setVisible(false);
        tituloField.setVisible(false);
        especialidadLabel.setVisible(false);
        especialidadField.setVisible(false);

        // Poblar carreras
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_carrera, nombre FROM Carrera ORDER BY nombre");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                carreraComboBox.getItems().add(new CarreraItem(rs.getInt("id_carrera"), rs.getString("nombre")));
            }
        } catch (Exception e) {
            showError("Error al cargar carreras: " + e.getMessage());
        }
        if (!carreraComboBox.getItems().isEmpty()) {
            carreraComboBox.setValue(carreraComboBox.getItems().get(0));
        }

        // Poblar materias según carrera seleccionada
        carreraComboBox.setOnAction(ev -> {
            CarreraItem selected = carreraComboBox.getValue();
            materiasListView.getItems().clear();
            if (selected != null) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id_materia, codigo, nombre FROM Materia WHERE id_carrera = ? ORDER BY nombre")) {
                    pstmt.setInt(1, selected.id);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        materiasListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("codigo"), rs.getString("nombre")));
                    }
                } catch (Exception e) {
                    showError("Error al cargar materias: " + e.getMessage());
                }
            }
        });
        // Trigger inicial
        carreraComboBox.getOnAction().handle(null);

        // Poblar todas las materias para profesor
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id_materia, codigo, nombre FROM Materia ORDER BY nombre");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                materiasProfesorListView.getItems().add(new MateriaItem(rs.getInt("id_materia"), rs.getString("codigo"), rs.getString("nombre")));
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
        }

        // Cambiar campos según rol
        rolComboBox.setOnAction(ev -> {
            String rol = rolComboBox.getValue();
            boolean esEstudiante = "ESTUDIANTE".equals(rol);
            boolean esProfesor = "PROFESOR".equals(rol);
            carreraLabel.setVisible(esEstudiante);
            carreraComboBox.setVisible(esEstudiante);
            materiasLabel.setVisible(esEstudiante);
            materiasListView.setVisible(esEstudiante);
            materiasProfesorLabel.setVisible(esProfesor);
            materiasProfesorListView.setVisible(esProfesor);
            tituloLabel.setVisible(esProfesor);
            tituloField.setVisible(esProfesor);
            especialidadLabel.setVisible(esProfesor);
            especialidadField.setVisible(esProfesor);
        });
        // Trigger inicial
        rolComboBox.getOnAction().handle(null);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.add(new Label("Cédula:"), 0, row); grid.add(cedulaField, 1, row++);
        grid.add(new Label("Nombre:"), 0, row); grid.add(nombreField, 1, row++);
        grid.add(new Label("Apellido:"), 0, row); grid.add(apellidoField, 1, row++);
        grid.add(new Label("Contraseña:"), 0, row); grid.add(passwordField, 1, row++);
        grid.add(new Label("Rol:"), 0, row); grid.add(rolComboBox, 1, row++);
        grid.add(carreraLabel, 0, row); grid.add(carreraComboBox, 1, row++);
        grid.add(materiasLabel, 0, row); grid.add(materiasListView, 1, row++);
        grid.add(materiasProfesorLabel, 0, row); grid.add(materiasProfesorListView, 1, row++);
        grid.add(tituloLabel, 0, row); grid.add(tituloField, 1, row++);
        grid.add(especialidadLabel, 0, row); grid.add(especialidadField, 1, row++);

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
                String titulo = tituloField.getText().trim();
                String especialidad = especialidadField.getText().trim();
                // Validación básica
                if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || password.isEmpty() || rol == null) {
                    showError("Todos los campos obligatorios deben estar llenos.");
                    return null;
                }
                if (!validarCedulaEcuatoriana(cedula)) {
                    showError("Cédula ecuatoriana no válida.");
                    return null;
                }
                return new UsuarioInput(cedula, nombre, apellido, password, rol, carrera, materiasEst, materiasProf, titulo, especialidad);
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
                            String sqlEst = "INSERT INTO Estudiante (id_usuario, matricula, id_carrera) VALUES (?, ?, ?)";
                            try (PreparedStatement pstmtEst = conn.prepareStatement(sqlEst, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                String matricula = generarMatricula(conn, input.getCarrera().id);
                                pstmtEst.setInt(1, idUsuario);
                                pstmtEst.setString(2, matricula);
                                pstmtEst.setInt(3, input.getCarrera().id);
                                pstmtEst.executeUpdate();
                                // Inscribir materias (opcional: tabla Inscripcion)
                                // ...
                            }
                        } else if ("PROFESOR".equals(input.getRol())) {
                            // Insertar en Profesor
                            String sqlProf = "INSERT INTO Profesor (id_usuario, titulo, especialidad) VALUES (?, ?, ?)";
                            try (PreparedStatement pstmtProf = conn.prepareStatement(sqlProf, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                pstmtProf.setInt(1, idUsuario);
                                pstmtProf.setString(2, input.getTitulo());
                                pstmtProf.setString(3, input.getEspecialidad());
                                pstmtProf.executeUpdate();
                                // Asignar materias (opcional: tabla Curso)
                                // ...
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

    // Utilidad para generar matrícula única
    private String generarMatricula(Connection conn, int idCarrera) throws Exception {
        String sql = "SELECT COUNT(*) FROM Estudiante WHERE id_carrera = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCarrera);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("%d%04d", idCarrera, count);
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
    private static class CarreraItem {
        int id;
        String nombre;
        CarreraItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
    private static class MateriaItem {
        int id;
        String codigo;
        String nombre;
        MateriaItem(int id, String codigo, String nombre) { this.id = id; this.codigo = codigo; this.nombre = nombre; }
        @Override public String toString() { return codigo + " - " + nombre; }
    }
    public static class UsuarioInput {
        private final String cedula, nombre, apellido, password, rol, titulo, especialidad;
        private final CarreraItem carrera;
        private final java.util.List<MateriaItem> materiasEstudiante;
        private final java.util.List<MateriaItem> materiasProfesor;
        public UsuarioInput(String cedula, String nombre, String apellido, String password, String rol, CarreraItem carrera, java.util.List<MateriaItem> materiasEstudiante, java.util.List<MateriaItem> materiasProfesor, String titulo, String especialidad) {
            this.cedula = cedula; this.nombre = nombre; this.apellido = apellido; this.password = password; this.rol = rol; this.carrera = carrera;
            this.materiasEstudiante = materiasEstudiante == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasEstudiante);
            this.materiasProfesor = materiasProfesor == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(materiasProfesor);
            this.titulo = titulo; this.especialidad = especialidad;
        }
        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getPassword() { return password; }
        public String getRol() { return rol; }
        public CarreraItem getCarrera() { return carrera; }
        public java.util.List<MateriaItem> getMateriasEstudiante() { return materiasEstudiante; }
        public java.util.List<MateriaItem> getMateriasProfesor() { return materiasProfesor; }
        public String getTitulo() { return titulo; }
        public String getEspecialidad() { return especialidad; }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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
        
        public UsuarioRow(int idUsuario, String cedula, String nombre, String apellido, String rol) {
            this.idUsuario = idUsuario;
            this.cedula = cedula;
            this.nombre = nombre;
            this.apellido = apellido;
            this.rol = rol;
        }
        
        public int getIdUsuario() { return idUsuario; }
        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getRol() { return rol; }
        
        public void setCedula(String cedula) { this.cedula = cedula; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public void setRol(String rol) { this.rol = rol; }
    }
} 