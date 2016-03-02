package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.collect.Maps;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcQuery;
import io.aiur.oss.db.jdbc.jdbc.impl.JdbcRepositoryImpl;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import other.AutowireUtil;
import other.ProjectionService;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

//@Slf4j
public class JdbcQueryLookupStrategy implements QueryLookupStrategy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcQueryLookupStrategy.class);

    private final EvaluationContextProvider evaluationContextProvider;

    @Inject
    private SqlCache sqlCache;

    @Inject
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Inject
    private ApplicationContext ctx;

    @Inject
    private ProjectionService projectionService;

    public JdbcQueryLookupStrategy(EvaluationContextProvider evaluationContextProvider) {
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
        return new RepositoryQuery(){

            @Override
            public Object execute(Object[] parameters) {
                String clazz = method.getDeclaringClass().getSimpleName();
                JdbcQuery ann = method.getAnnotation(JdbcQuery.class);
                Assert.notNull(ann, "Could not find @JdbcQuery on custom query for " + clazz + "#" + method.getName());


                Class<?>[] types = GenericTypeResolver.resolveTypeArguments(method.getDeclaringClass(), Repository.class);
                Class<?> repoType = types[0];
                Class<?> idType = types[1];



                String sql;
                if( StringUtils.hasText(ann.value()) ){
                    sql = sqlCache.getByKey(ann.value());
                }else{
                    sql = ann.query();
                }
                if( sql == null ){

                    log.warn("Could not determine query for {}#{} with annotation {}",
                            clazz, method.getName(), ann);
                    throw new IllegalArgumentException("Could not determine query for custom method " + clazz + "#" + method.getName());
                }

                // by default, return all results
                Function<List<?>, ?> processor = (o) -> o;


                Map<String, Object>  namedParams = Maps.newHashMap();

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




                Class<?> returnType;
                Class<?> methodType = method.getReturnType();
                boolean isOptional = Optional.class.equals(methodType);


                if( parameters.length > 0 && parameters[parameters.length -1] instanceof Pageable ){
                    Pageable pageable = (Pageable) parameters[parameters.length -1];
                    parameters = Arrays.copyOfRange(parameters, 0, parameters.length -1);


                    String countQuery;
                    if( StringUtils.hasText(ann.countKey()) ){
                        countQuery = sqlCache.getByKey(ann.countKey());
                    }else{
                        countQuery = ann.countQuery();
                    }

                    Assert.hasText(countQuery, "Could not determine count query for " + ann + " on method " + method);

                    Long totalElements = jdbcTemplate.queryForObject(countQuery, namedParams, Long.class);


                    // TODO would be nicer if some of this was public. Open a PR
                    Object repo = ctx.getBean(metadata.getRepositoryInterface());
                    while(AopUtils.isJdkDynamicProxy(repo)){
                        repo = unwrapProxy(repo);
                    }
                    JdbcRepositoryImpl impl = (JdbcRepositoryImpl) repo;
                    SqlGenerator gen = impl.getSqlGenerator();
                    Method limit = ReflectionUtils.findMethod(gen.getClass(), "limitClause", Pageable.class);
                    ReflectionUtils.makeAccessible(limit);
                    String limitClause = (String) ReflectionUtils.invokeMethod(limit, gen, pageable);

                    sql += " " + limitClause;

                    processor = (results) -> new PageImpl(results, pageable, totalElements);
                } else if (isOptional ){
                    processor = (results) -> results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
                } else if( Collection.class.isAssignableFrom( methodType ) == false) {
                    processor = (results) -> results.isEmpty() ? null : results.get(0);
                }




                if( isOptional ){
                    if( ann.beanType().equals(Class.class) ){
                        returnType = repoType;
                    }else{
                        returnType = ann.beanType();
                    }
                }else{
                    returnType = ann.beanType().equals(Class.class) ? method.getReturnType() : ann.beanType();
                }



                RowMapper<?> rowMapper = JdbcRepositoryImpl.resolveRowMapper(returnType);
                AutowireUtil.autowire(rowMapper);
                List<?> results = jdbcTemplate.query(sql, namedParams, rowMapper);

                Object processed = processor.apply(results);


                if( !ann.projection().equals(Class.class) ){
                    processed = projectionService.convert(processed, ann.projection());
                }

                return processed;
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

            @Override
            public QueryMethod getQueryMethod() {
                return new QueryMethod(method, metadata);
            }
        };
    }
}