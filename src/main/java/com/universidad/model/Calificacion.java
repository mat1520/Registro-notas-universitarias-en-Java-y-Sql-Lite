package com.universidad.model;

public class Calificacion {
    private Integer idCalificacion;
    private Estudiante estudiante;
    private Materia materia;

    public Calificacion() {}

    public Calificacion(Integer idCalificacion, Estudiante estudiante, Materia materia) {
        this.idCalificacion = idCalificacion;
        this.estudiante = estudiante;
        this.materia = materia;
    }

    public Integer getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Integer idCalificacion) { this.idCalificacion = idCalificacion; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
} 