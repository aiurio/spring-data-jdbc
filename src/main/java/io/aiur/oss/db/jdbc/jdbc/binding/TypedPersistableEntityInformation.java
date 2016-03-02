package io.aiur.oss.db.jdbc.jdbc.binding;

import lombok.Getter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.EntityInformation;

import java.io.Serializable;

@Getter
public class TypedPersistableEntityInformation<T extends Persistable<ID>, ID extends Serializable>
        implements EntityInformation<T, ID> {

    private final Class<ID> idType;
    private final Class<T> javaType;

    public TypedPersistableEntityInformation(Class<ID> idType, Class<T> javaType) {
        this.idType = idType;
        this.javaType = javaType;
    }

    @Override
    public boolean isNew(T entity) {
        return entity.getId() == null;
    }

    @Override
    public ID getId(T entity) {
        return entity.getId();
    }

}
