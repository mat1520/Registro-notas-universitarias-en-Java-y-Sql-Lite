 package com.universidad.dao;

import java.util.List;

import com.universidad.model.Estudiante;

public interface EstudianteDAO extends BaseDAO<Estudiante> {
    Estudiante findByMatricula(String matricula) throws Exception;
    List<Estudiante> findByCarrera(Integer idCarrera) throws Exception;
} 