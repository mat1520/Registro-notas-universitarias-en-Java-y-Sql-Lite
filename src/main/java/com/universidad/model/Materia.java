package com.universidad.model;

public class Materia {
    private Integer idMateria;
    private String nombre_materia;
    private Carrera carrera;

    public Materia() {}

    public Materia(Integer idMateria, String nombre_materia, Carrera carrera) {
        this.idMateria = idMateria;
        this.nombre_materia = nombre_materia;
        this.carrera = carrera;
    }

    // Getters y Setters
    public Integer getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Integer idMateria) {
        this.idMateria = idMateria;
    }

    public String getNombre_materia() {
        return nombre_materia;
    }

    public void setNombre_materia(String nombre_materia) {
        this.nombre_materia = nombre_materia;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    @Override
    public String toString() {
        return nombre_materia;
    }
}