package com.universidad.model;

public class Materia {
    private Integer idMateria;
    private String codigo;
    private String nombre;
    private Integer creditos;
    private Carrera carrera;

    public Materia() {}

    public Materia(Integer idMateria, String codigo, String nombre, Integer creditos, Carrera carrera) {
        this.idMateria = idMateria;
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.carrera = carrera;
    }

    // Getters y Setters
    public Integer getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Integer idMateria) {
        this.idMateria = idMateria;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreditos() {
        return creditos;
    }

    public void setCreditos(Integer creditos) {
        this.creditos = creditos;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
} 