package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.base.CaseFormat;
import io.aiur.oss.db.jdbc.jdbc.JdbcRepository;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcEntity;
import io.aiur.oss.db.jdbc.jdbc.impl.JdbcRepositoryImpl;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.util.StringUtils;

import java.io.Serializable;

public class TypedRepositoryFactorySupport<T extends Persistable<ID>, ID extends Serializable> extends RepositoryFactorySupport {

    private final Class<?> repositoryBaseClass;
    private final Class<ID> idType;
    private final Class<T> domainType;
    private final AutowireCapableBeanFactory beanFactory;


    public TypedRepositoryFactorySupport(Class idType, Class domainType,
                                         Class<?> repositoryBaseClass, AutowireCapableBeanFactory beanFactory) {
        this.repositoryBaseClass = repositoryBaseClass;
        this.idType = idType;
        this.domainType = domainType;
        this.beanFactory = beanFactory;
    }


    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return new TypedPersistableEntityInformation(idType, domainClass);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation md) {
        Class<?> id = md.getIdType() == null ? idType : md.getIdType();
        Class<?> domain = md.getDomainType() == null ? domainType : md.getDomainType();


        // does this entity override the default schema
        String table = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, domain.getSimpleName());
        JdbcEntity a = domain.getAnnotation(JdbcEntity.class);
        if( a != null ){
            String schema = StringUtils.hasText(a.schema()) ? a.schema() : null;
            table = StringUtils.hasText(a.table()) ? a.table() : table;
            if( schema != null ){
                table = schema + "." + table;
            }
        }

        JdbcRepositoryImpl<?, ?> repo = new JdbcRepositoryImpl(id, domain, table, "id");
        beanFactory.autowireBean(repo);
        try {
            repo.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (Repository) repo;
    }


    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return repositoryBaseClass;
    }

    @Override
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        if( JdbcRepository.class.isAssignableFrom(repositoryInterface)
                && !JdbcRepository.class.equals(repositoryInterface)){
            Class<?>[] types = GenericTypeResolver.resolveTypeArguments(repositoryInterface, JdbcRepository.class);
            if( types != null && types.length >= 2 ){
                return new TypedRepositoryMetadata((Class<? extends Serializable>) types[1], types[0], repositoryInterface, true);
            }
        }

        return new TypedRepositoryMetadata(idType, domainType, repositoryInterface, true);
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        JdbcQueryLookupStrategy strategy = new JdbcQueryLookupStrategy(evaluationContextProvider);
        beanFactory.autowireBean(strategy);
        return strategy;
    }




}