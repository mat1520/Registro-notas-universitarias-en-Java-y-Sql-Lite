package com.universidad.model;

public class Usuario {
    private Integer idUsuario;
    private String cedula;
    private String nombre;
    private String apellido;
    private String password;
    private String rol;

    public Usuario() {}

    public Usuario(Integer idUsuario, String cedula, String nombre, String apellido, String password, String rol) {
        this.idUsuario = idUsuario;
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.password = password;
        this.rol = rol;
    }

    // Getters y Setters
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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
        return nombre + " " + apellido;
    }
} 