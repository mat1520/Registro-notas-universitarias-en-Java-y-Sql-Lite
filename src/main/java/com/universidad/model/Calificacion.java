package com.universidad.model;

public class Calificacion {
    private Integer idCalificacion;
    private Estudiante estudiante;
    private Curso curso;

    public Calificacion() {}

    public Calificacion(Integer idCalificacion, Estudiante estudiante, Curso curso) {
        this.idCalificacion = idCalificacion;
        this.estudiante = estudiante;
        this.curso = curso;
    }

    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
} 