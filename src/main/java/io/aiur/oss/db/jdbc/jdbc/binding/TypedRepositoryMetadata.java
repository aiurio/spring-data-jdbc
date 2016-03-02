package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultCrudMethods;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

@Getter
public class TypedRepositoryMetadata implements RepositoryMetadata {

    private final Class<? extends Serializable> idType;
    private final Class<?> domainType, repositoryInterface;
    private final boolean pagingRepository;

    private Set<Class<?>> alternativeDomainTypes = Sets.newHashSet();

    public TypedRepositoryMetadata(Class<? extends Serializable> idType, Class<?> domainType,
                                   Class<?> repositoryInterface, boolean pagingRepository) {
        this.idType = idType;
        this.domainType = domainType;
        this.repositoryInterface = repositoryInterface;
        this.pagingRepository = pagingRepository;
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {
        return method.getReturnType();
    }

    @Override
    public CrudMethods getCrudMethods() {
        return new DefaultCrudMethods(this);
    }
}