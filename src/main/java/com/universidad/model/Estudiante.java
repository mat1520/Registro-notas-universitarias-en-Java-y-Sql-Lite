package com.universidad.model;

public class Estudiante {
    private Integer idEstudiante;
    private Usuario usuario;

    public Estudiante() {}

    public Estudiante(Integer idEstudiante, Usuario usuario) {
        this.idEstudiante = idEstudiante;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return usuario.getNombre_usuario() + " " + usuario.getApellido_usuario();
    }
} 