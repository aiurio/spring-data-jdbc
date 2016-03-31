package io.aiur.oss.db.jdbc.jdbc.binding;

import com.google.common.collect.Lists;
import io.aiur.oss.db.jdbc.jdbc.convert.JdbcTypeConverter;
import io.aiur.oss.db.jdbc.jdbc.mapping.SqlCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import io.aiur.oss.db.jdbc.jdbc.convert.ProjectionService;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;

//@Slf4j
public class JdbcQueryLookupStrategy implements QueryLookupStrategy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcQueryLookupStrategy.class);

    private final EvaluationContextProvider evaluationContextProvider;

    @Inject @Lazy
    private SqlCache sqlCache;

    @Inject
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Inject
    private ApplicationContext ctx;

    @Inject
    private ProjectionService projectionService;

    @Inject
    private List<JdbcTypeConverter> converters = Lists.newArrayList();

    public JdbcQueryLookupStrategy(EvaluationContextProvider evaluationContextProvider) {
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
        return new JdbcRepositoryQuery(method, metadata, namedQueries, ctx, projectionService, jdbcTemplate, sqlCache, converters);
    }
}