package com.universidad.model;

public class Profesor {
    private Integer idProfesor;
    private Usuario usuario;
    private String nombre_profesor;

    public Profesor() {}

    public Profesor(Integer idProfesor, Usuario usuario, String nombre_profesor) {
        this.idProfesor = idProfesor;
        this.usuario = usuario;
        this.nombre_profesor = nombre_profesor;
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

    public String getNombre_profesor() {
        return nombre_profesor;
    }

    public void setNombre_profesor(String nombre_profesor) {
        this.nombre_profesor = nombre_profesor;
    }

    @Override
    public String toString() {
        return usuario.getNombre_usuario() + " " + usuario.getApellido_usuario();
    }
} 