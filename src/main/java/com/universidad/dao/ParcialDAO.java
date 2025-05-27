package com.universidad.dao;
import java.util.List;

import com.universidad.model.Parcial;

public interface ParcialDAO {
    List<Parcial> findAll() throws Exception;
    Parcial findById(int id) throws Exception;
} 