package info.shillem.dao;

import java.util.List;

import info.shillem.dao.lang.DaoException;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;

public interface GenericDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    default List<T> collect(Query<E> query) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default void create(T dto) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default void delete(List<T> dtos) throws DaoException {
        throw new UnsupportedOperationException();
    }
    
    default void delete(Query<E> query) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default T read(Query<E> query) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default void update(List<T> dtos) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default void update(T dto) throws DaoException {
        throw new UnsupportedOperationException();
    }

    default void upsert(List<T> dtos) throws DaoException {
        throw new UnsupportedOperationException();
    }

}
