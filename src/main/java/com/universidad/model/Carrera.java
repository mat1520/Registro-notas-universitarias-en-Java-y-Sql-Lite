package com.universidad.model;

public class Carrera {
    private Integer idCarrera;
    private String nombre_carrera;

    public Carrera() {}

    public Carrera(Integer idCarrera, String nombre_carrera) {
        this.idCarrera = idCarrera;
        this.nombre_carrera = nombre_carrera;
    }

    // Getters y Setters
    public Integer getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre_carrera() {
        return nombre_carrera;
    }

    public void setNombre_carrera(String nombre_carrera) {
        this.nombre_carrera = nombre_carrera;
    }

    @Override
    public String toString() {
        return nombre_carrera;
    }
} 