package com.universidad.dao;

import java.util.List;

import com.universidad.model.Materia;

public interface MateriaDAO extends BaseDAO<Materia> {
    List<Materia> findByCarrera(Integer idCarrera) throws Exception;
} 