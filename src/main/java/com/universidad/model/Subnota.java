package com.universidad.model;

public class Subnota {
    private Integer idSubnota;
    private Integer idCalificacion;
    private Integer idParcialBaseDatos;
    private Integer numero;
    private Double valor;

    public Subnota() {}

    public Subnota(Integer idSubnota, Integer idCalificacion, Integer idParcialBaseDatos, Integer numero, Double valor) {
        this.idSubnota = idSubnota;
        this.idCalificacion = idCalificacion;
        this.idParcialBaseDatos = idParcialBaseDatos;
        this.numero = numero;
        this.valor = valor;
    }

    public Integer getIdSubnota() { return idSubnota; }
    public void setIdSubnota(Integer idSubnota) { this.idSubnota = idSubnota; }
    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }
    public Integer getIdParcialBaseDatos() { return idParcialBaseDatos; }
    public void setIdParcialBaseDatos(Integer idParcialBaseDatos) { this.idParcialBaseDatos = idParcialBaseDatos; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
} 