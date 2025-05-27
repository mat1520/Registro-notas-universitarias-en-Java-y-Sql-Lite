package com.universidad.model;

public class Carrera {
    private Integer idCarrera;
    private String nombre;
    private String descripcion;
    private Facultad facultad;

    public Carrera() {}

    public Carrera(Integer idCarrera, String nombre, String descripcion, Facultad facultad) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.facultad = facultad;
    }

    // Getters y Setters
    public Integer getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    @Override
    public String toString() {
        return nombre;
    }
} 