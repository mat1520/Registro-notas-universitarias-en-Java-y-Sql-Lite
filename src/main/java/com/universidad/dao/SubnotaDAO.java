package com.universidad.dao;
import java.util.List;

import com.universidad.model.Subnota;

public interface SubnotaDAO {
    List<Subnota> findByCalificacion(int idCalificacion) throws Exception;
    List<Subnota> findByCalificacionAndParcial(int idCalificacion, int idParcial) throws Exception;
    void save(Subnota subnota) throws Exception;
} 