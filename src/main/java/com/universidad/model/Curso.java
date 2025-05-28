package com.universidad.model;

public class Curso {
    private Integer idCurso;
    private Materia materia;
    private Profesor profesor;

    public Curso() {}

    public Curso(Integer idCurso, Materia materia, Profesor profesor) {
        this.idCurso = idCurso;
        this.materia = materia;
        this.profesor = profesor;
    }

    public Integer getIdCurso() { return idCurso; }
    public void setIdCurso(Integer idCurso) { this.idCurso = idCurso; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    @Override
    public String toString() {
        return materia.getNombre() + " - " + profesor.getUsuario().getNombre();
    }
} 