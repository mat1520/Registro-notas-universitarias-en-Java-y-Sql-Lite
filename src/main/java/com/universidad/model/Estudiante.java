package com.universidad.model;

public class Estudiante {
    private Integer idEstudiante;
    private Usuario usuario;
    private Carrera carrera;

    public Estudiante() {}

    public Estudiante(Integer idEstudiante, Usuario usuario, Carrera carrera) {
        this.idEstudiante = idEstudiante;
        this.usuario = usuario;
        this.carrera = carrera;
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

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    @Override
    public String toString() {
        return usuario.getNombre_usuario() + " " + usuario.getApellido_usuario();
    }
} 