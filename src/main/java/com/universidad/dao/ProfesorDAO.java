package com.universidad.dao;

import java.util.List;

import com.universidad.model.Profesor;

public interface ProfesorDAO extends BaseDAO<Profesor> {
    Profesor findByCedula(String cedula) throws Exception;
    List<Profesor> findByEspecialidad(String especialidad) throws Exception;
} 