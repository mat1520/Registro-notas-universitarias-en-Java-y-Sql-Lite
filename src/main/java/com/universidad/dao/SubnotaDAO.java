package com.universidad.dao;

import java.util.List;

import com.universidad.model.Subnota;

public interface SubnotaDAO {
    Subnota create(Subnota subnota) throws Exception;
    Subnota read(Integer id) throws Exception;
    List<Subnota> findByCalificacion(Integer idCalificacion) throws Exception;
    Subnota update(Subnota subnota) throws Exception;
    void delete(Integer id) throws Exception;
} 