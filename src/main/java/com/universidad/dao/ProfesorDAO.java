package com.universidad.dao;

import java.util.List;

import com.universidad.model.Profesor;

public interface ProfesorDAO {
    Profesor create(Profesor profesor) throws Exception;
    Profesor read(Integer id) throws Exception;
    List<Profesor> readAll() throws Exception;
    Profesor update(Profesor profesor) throws Exception;
    void delete(Integer id) throws Exception;
    Profesor findByCedula(String cedula) throws Exception;
    List<Profesor> findByEspecialidad(String especialidad) throws Exception;
} 