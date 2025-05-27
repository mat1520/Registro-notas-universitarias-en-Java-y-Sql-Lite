package com.universidad.dao;

import java.util.List;

import com.universidad.model.Materia;

public interface MateriaDAO extends BaseDAO<Materia> {
    Materia findByCodigo(String codigo) throws Exception;
    List<Materia> findByCarrera(Integer idCarrera) throws Exception;
} 