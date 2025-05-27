package com.universidad.dao;

import java.util.List;

public interface BaseDAO<T> {
    T create(T entity) throws Exception;
    T read(Integer id) throws Exception;
    List<T> readAll() throws Exception;
    T update(T entity) throws Exception;
    void delete(Integer id) throws Exception;
} 