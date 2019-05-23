package info.shillem.dao;

import java.util.List;

import info.shillem.dao.lang.DaoException;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;

public interface GenericDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {
    
    List<T> collect(FilterQuery<E> query) throws DaoException;

    void create(T dto) throws DaoException;

    void delete(Query<E> query) throws DaoException;

    T read(Query<E> query) throws DaoException;

    void update(List<T> dto) throws DaoException;

    void update(T dto) throws DaoException;

}
