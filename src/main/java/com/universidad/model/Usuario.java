package com.universidad.model;

public class Usuario {
    private Integer idUsuario;
    private String cedula;
    private String nombre_usuario;
    private String apellido_usuario;
    private String password;
    private String rol;

    public Usuario() {}

    public Usuario(Integer idUsuario, String cedula, String nombre_usuario, String apellido_usuario, String password, String rol) {
        this.idUsuario = idUsuario;
        this.cedula = cedula;
        this.nombre_usuario = nombre_usuario;
        this.apellido_usuario = apellido_usuario;
        this.password = password;
        this.rol = rol;
    }

    
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getApellido_usuario() {
        return apellido_usuario;
    }

    public void setApellido_usuario(String apellido_usuario) {
        this.apellido_usuario = apellido_usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return nombre_usuario + " " + apellido_usuario;
    }
} 