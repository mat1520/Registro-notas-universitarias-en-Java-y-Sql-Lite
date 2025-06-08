package com.universidad.dao;

import java.util.List;

import com.universidad.model.Materia;

public interface MateriaDAO {
    Materia create(Materia materia) throws Exception;
    Materia read(Integer id) throws Exception;
    List<Materia> readAll() throws Exception;
    Materia update(Materia materia) throws Exception;
    void delete(Integer id) throws Exception;
    List<Materia> findByCarrera(Integer idCarrera) throws Exception;
} 