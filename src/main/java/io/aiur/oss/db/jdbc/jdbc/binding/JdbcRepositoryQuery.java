package io.aiur.oss.db.jdbc.jdbc.binding;


import com.google.common.collect.Maps;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcQuery;
import io.aiur.oss.db.jdbc.jdbc.impl.JdbcRepositoryImpl;
import io.aiur.oss.db.jdbc.jdbc.mapping.RowMappers;
import io.aiur.oss.db.jdbc.jdbc.mapping.SqlCache;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import other.AutowireUtil;
import other.ProjectionService;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class JdbcRepositoryQuery implements RepositoryQuery {

    private final Method method;
    private final RepositoryMetadata metadata;
    private final NamedQueries namedQueries;
    private final Class<?> repoType, idType;
    private final ApplicationContext ctx;
    private final ProjectionService projectionService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlCache sqlCache;

    public JdbcRepositoryQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries,
                               ApplicationContext ctx, ProjectionService projectionService,
                               NamedParameterJdbcTemplate jdbcTemplate, SqlCache sqlCache) {
        this.method = method;
        this.metadata = metadata;
        this.namedQueries = namedQueries;

        this.ctx = ctx;
        this.projectionService = projectionService;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlCache = sqlCache;

        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(method.getDeclaringClass(), Repository.class);
        repoType = types[0];
        idType = types[1];
    }

    @Override
    public QueryMethod getQueryMethod() {
        return new QueryMethod(method, metadata);
    }


    /*
     we always query for a list, even if we only want a single result.
     once we get our result, run a "processor" function over it to get our desired return type.
     this function is responsible for handling custom types (Optional, Pageable, etc)
     */
    @Override
    public Object execute(Object[] parameters) {
        String clazz = method.getDeclaringClass().getSimpleName();
        JdbcQuery ann = method.getAnnotation(JdbcQuery.class);
        Assert.notNull(ann, "Could not find @JdbcQuery on custom query for " + clazz + "#" + method.getName());


        StringBuilder sql = JdbcQueryUtil.sqlBuilderFromMethod(method, sqlCache, true);
        Map<String, Object>  namedParams = getParams(parameters);

        Class<?> methodType = method.getReturnType();
        boolean isOptional = Optional.class.equals(methodType);
        boolean isCollection = Collection.class.isAssignableFrom( methodType );

        // by default, just return the results from the database
        Function<List<?>, ?> processor = (o) -> o;

        // handle custom types
        if( parameters.length > 0 && parameters[parameters.length -1] instanceof Pageable ){
            Pageable pageable = (Pageable) parameters[parameters.length -1];
            processor = createPageableProcessor(pageable, namedParams, ann);
            addPageableLimitClause(pageable, sql);
        } else if (isOptional ){
            processor = (results) -> results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } else if( !isCollection) {
            processor = (results) -> results.isEmpty() ? null : results.get(0);
        }


        Class<?> returnType = determineReturnType(isOptional, ann);
        RowMapper<?> rowMapper = RowMappers.resolveRowMapper(returnType);
        AutowireUtil.autowire(rowMapper);


        List<?> results = jdbcTemplate.query(sql.toString(), namedParams, rowMapper);

        Object processed = processor.apply(results);
        if( !ann.projection().equals(Class.class) ){
            processed = projectionService.convert(processed, ann.projection());
        }

        return processed;
    }



    private Class<?> determineReturnType(boolean isOptional, JdbcQuery ann) {
        Class<?> returnType;
        if( isOptional ){
            if( ann.beanType().equals(Class.class) ){
                returnType = repoType;
            }else{
                returnType = ann.beanType();
            }
        }else{
            returnType = ann.beanType().equals(Class.class) ? method.getReturnType() : ann.beanType();
        }
        return returnType;
    }

    private void addPageableLimitClause(Pageable pageable, StringBuilder sql) {
        Object repo = ctx.getBean(metadata.getRepositoryInterface());
        while(AopUtils.isJdkDynamicProxy(repo)){
            repo = unwrapProxy(repo);
        }
        JdbcRepositoryImpl impl = (JdbcRepositoryImpl) repo;
        SqlGenerator gen = impl.getSqlGenerator();
        String limitClause = gen.limitClause(pageable);
        sql.append(" " + limitClause);
    }

    private Function<List<?>, ?> createPageableProcessor(Pageable pageable, Map<String, Object> namedParams, JdbcQuery ann) {
        Long totalElements = queryTotalElements(ann, namedParams);
        Function<List<?>, ?> processor = (results) -> new PageImpl(results, pageable, totalElements);
        return processor;
    }

    private Long queryTotalElements(JdbcQuery ann, Map<String, Object> namedParams) {
        String countQuery;
        if( StringUtils.hasText(ann.countKey()) ){
            countQuery = sqlCache.getByKey(ann.countKey());
        }else{
            countQuery = ann.countQuery();
        }

        Assert.hasText(countQuery, "Could not determine count query for " + ann + " on method " + method);
        Long totalElements = jdbcTemplate.queryForObject(countQuery, namedParams, Long.class);
        return totalElements;
    }

    private Map<String, Object> getParams(Object[] parameters) {
        HashMap<String, Object> namedParams = Maps.newHashMap();

        Annotation[][] pann = method.getParameterAnnotations();
        for(int i = 0; i < parameters.length; i++){
            if( pann[i].length > 0){
                for(Annotation a : pann[i]){
                    if( a instanceof Param ){
                        namedParams.put(((Param) a).value(), parameters[i]);
                    }
                }
            }
        }
        return namedParams;
    }


    private Object unwrapProxy(Object proxy) {
        try {
            if (AopUtils.isJdkDynamicProxy(proxy)) {
                return ((Advised) proxy).getTargetSource().getTarget();
            }else{
                return proxy;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not unwrap proxy", e);
        }
    }

}