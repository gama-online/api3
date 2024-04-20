package lt.gama.model.mappers;

import org.mapstruct.Mapper;

public class DummyMapper<T> implements IBaseMapper<T, T> {
    @Override
    public T toDto(T entity) {
        return entity;
    }

    @Override
    public T toEntity(T dto) {
        return dto;
    }
}
