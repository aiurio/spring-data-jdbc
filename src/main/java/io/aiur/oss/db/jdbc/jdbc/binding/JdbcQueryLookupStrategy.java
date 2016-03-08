package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.collect.Maps;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcQuery;
import io.aiur.oss.db.jdbc.jdbc.impl.JdbcRepositoryImpl;
import io.aiur.oss.db.jdbc.jdbc.mapping.RowMappers;
import io.aiur.oss.db.jdbc.jdbc.mapping.SqlCache;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql.SqlGenerator;
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
        return new JdbcRepositoryQuery(method, metadata, namedQueries, ctx, projectionService, jdbcTemplate, sqlCache);
    }
}