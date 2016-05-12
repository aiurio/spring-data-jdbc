package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.collect.Sets;
import io.aiur.oss.db.jdbc.jdbc.JdbcRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import java.beans.PropertyDescriptor;
import java.io.Serializable;

/**
 * Created by dave on 12/16/15.
 */
@Slf4j
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
        BasicPersistentEntity basic = new BasicPersistentEntity(typeInfo);

        // find the ID field
        ReflectionUtils.doWithFields(domainType, (field)->{
            if( basic.hasIdProperty() ){
                log.warn("Found multiple ID properties for {}: {} and {}",
                        domainType, basic.getIdProperty().getField().getType(), field.getType());
            }else{
                try {
                    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), domainType);
                    SimpleTypeHolder sth = new SimpleTypeHolder(Sets.newHashSet(field.getType()), true);
                    PersistentProperty prop = new AnnotationBasedPersistentProperty(field, pd, basic, sth) {
                        @Override
                        protected Association createAssociation() {
                            return null;
                        }
                    };
                    basic.addPersistentProperty(prop);
                }catch(Exception e){
                    log.warn("Failed adding PersistentProperty ID ", e);
                }
            }
        }, (matchField) -> matchField.getDeclaredAnnotation(Id.class) != null);

        return basic;
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new TypedRepositoryFactorySupport(idType, domainType, JdbcRepository.class, beanFactory);
    }
}
