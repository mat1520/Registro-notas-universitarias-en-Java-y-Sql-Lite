package com.universidad.model;

public class Subnota {
    private Integer idSubnota;
    private Integer idCalificacion;
    private Integer idParcial;
    private int numero;
    private double valor;
    public Subnota() {}
    public Subnota(Integer idSubnota, Integer idCalificacion, Integer idParcial, int numero, double valor) {
        this.idSubnota = idSubnota;
        this.idCalificacion = idCalificacion;
        this.idParcial = idParcial;
        this.numero = numero;
        this.valor = valor;
    }
    public Integer getIdSubnota() { return idSubnota; }
    public void setIdSubnota(Integer idSubnota) { this.idSubnota = idSubnota; }
    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }
    public Integer getIdParcial() { return idParcial; }
    public void setIdParcial(Integer idParcial) { this.idParcial = idParcial; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
} 