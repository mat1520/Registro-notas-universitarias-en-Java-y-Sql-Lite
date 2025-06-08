package com.universidad.dao;

import java.util.List;

import com.universidad.model.Estudiante;

public interface EstudianteDAO {
    Estudiante create(Estudiante estudiante) throws Exception;
    Estudiante read(Integer id) throws Exception;
    List<Estudiante> readAll() throws Exception;
    Estudiante update(Estudiante estudiante) throws Exception;
    void delete(Integer id) throws Exception;
    List<Estudiante> findByCarrera(Integer idCarrera) throws Exception;
} 