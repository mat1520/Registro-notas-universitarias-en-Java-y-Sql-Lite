package com.universidad.dao;

import java.util.List;

import com.universidad.model.Estudiante;

public interface EstudianteDAO extends BaseDAO<Estudiante> {
    List<Estudiante> findByCarrera(Integer idCarrera) throws Exception;
} 