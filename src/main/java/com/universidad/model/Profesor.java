package com.universidad.model;

public class Profesor {
    private Integer idProfesor;
    private Usuario usuario;
    private String titulo;
    private String especialidad;

    public Profesor() {}

    public Profesor(Integer idProfesor, Usuario usuario, String titulo, String especialidad) {
        this.idProfesor = idProfesor;
        this.usuario = usuario;
        this.titulo = titulo;
        this.especialidad = especialidad;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String toString() {
        return usuario.getNombre() + " " + usuario.getApellido() + " - " + titulo;
    }
} 