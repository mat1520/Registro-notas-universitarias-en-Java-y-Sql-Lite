package com.universidad.model;

public class Materia {
    private Integer idMateria;
    private String nombre;
    private Carrera carrera;

    public Materia() {}

    public Materia(Integer idMateria, String nombre, Carrera carrera) {
        this.idMateria = idMateria;
        this.nombre = nombre;
        this.carrera = carrera;
    }

    // Getters y Setters
    public Integer getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Integer idMateria) {
        this.idMateria = idMateria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    @Override
    public String toString() {
        return nombre;
    }
} 