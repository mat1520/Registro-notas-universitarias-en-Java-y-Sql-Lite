package com.universidad.model;

public class Profesor {
    private Integer idProfesor;
    private Usuario usuario;

    public Profesor() {}

    public Profesor(Integer idProfesor, Usuario usuario) {
        this.idProfesor = idProfesor;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(Integer idProfesor) {
        this.idProfesor = idProfesor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return usuario.getNombre() + " " + usuario.getApellido();
    }
} 