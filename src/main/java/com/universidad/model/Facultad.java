package com.universidad.model;

import java.time.LocalDate;

public class Facultad {
    private Integer idFacultad;
    private String nombre;
    private String descripcion;
    private LocalDate fechaCreacion;

    public Facultad() {}

    public Facultad(Integer idFacultad, String nombre, String descripcion, LocalDate fechaCreacion) {
        this.idFacultad = idFacultad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Integer getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(Integer idFacultad) {
        this.idFacultad = idFacultad;
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

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombre;
    }
} 