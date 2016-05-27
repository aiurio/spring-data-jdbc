package io.aiur.oss.db.jdbc.jdbc.binding;

import io.aiur.oss.db.jdbc.jdbc.JdbcRepository;
import io.aiur.oss.db.jdbc.jdbc.mapping.JdbcPersistentEntityImpl;
import lombok.Setter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Created by dave on 12/16/15.
 */
public class JdbcRepositoryFactory<T extends Repository<S, ID>, S, ID extends Serializable> extends
        TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    @Setter
    private Class<ID> idType;

    @Setter
    private Class<S> domainType;

    @Inject
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setRepositoryInterface(Class<? extends T> repositoryInterface) {
        super.setRepositoryInterface(repositoryInterface);
        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(repositoryInterface, Repository.class);
        if( types != null && types.length >= 2 ){
            idType = (Class<ID>) types[1];
            domainType = (Class<S>) types[0];
        }
    }

    @Override
    public PersistentEntity<?, ?> getPersistentEntity() {
        TypeInformation<?> typeInfo = ClassTypeInformation.from(domainType);
        return new JdbcPersistentEntityImpl<>(typeInfo);
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new TypedRepositoryFactorySupport(idType, domainType, JdbcRepository.class, beanFactory);
    }
}
