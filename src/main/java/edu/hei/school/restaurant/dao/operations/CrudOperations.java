package edu.hei.school.restaurant.dao.operations;

import java.util.List;

public interface CrudOperations<E> {
    List<E> getAll(int page, int size);

    E findById(Long id);

    // Both create (if does not exist) or update (if exist) entities
    List<E> saveAll(List<E> entities);

}
