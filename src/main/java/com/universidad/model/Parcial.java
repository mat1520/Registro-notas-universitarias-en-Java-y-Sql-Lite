package com.universidad.model;

public class Parcial {
    private Integer idParcial;
    private String nombre;
    private double porcentaje;

    public Parcial() {}
    public Parcial(Integer idParcial, String nombre, double porcentaje) {
        this.idParcial = idParcial;
        this.nombre = nombre;
        this.porcentaje = porcentaje;
    }
    public Integer getIdParcial() { return idParcial; }
    public void setIdParcial(Integer idParcial) { this.idParcial = idParcial; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
    @Override
    public String toString() { return nombre; }
} 