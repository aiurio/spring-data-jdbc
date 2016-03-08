package io.aiur.oss.db.jdbc.jdbc.impl;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import io.aiur.oss.db.jdbc.jdbc.BasePersistable;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcEntity;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcMarshallers;
import io.aiur.oss.db.jdbc.jdbc.mapping.ColumnAwareBeanPropertyRowMapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.MissingRowUnmapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.RowUnmapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.TableDescription;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql.PostgreSqlGenerator;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql.SqlGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by dave on 12/15/15.
 */
@Slf4j
public class JdbcRepositoryImpl<T extends BasePersistable<ID>, ID extends Serializable>
        implements PagingAndSortingRepository<T, ID>, InitializingBean, ApplicationContextAware {

    // default to postges, for now. because we like postgres
    public static SqlGenerator DEFAULT_GENERATOR = new PostgreSqlGenerator();

    private static final List<Class<?>> PRIMITIVES = Arrays.asList(
            Long.class, Integer.class, Float.class, Double.class, String.class, Number.class, Boolean.class
    );

    private Class<ID> idType;

    @Getter
    private SqlGenerator sqlGenerator;

    @Getter
    private final TableDescription table;

    private final RowMapper<T> rowMapper;
    private final RowUnmapper<T> rowUnmapper;

    @Inject
    private JdbcOperations jdbcOperations;
    @Inject
    private ApplicationContext applicationContext;

    public JdbcRepositoryImpl(Class<ID> idType, Class<T> type, String table, String idColumn){
        this.idType = idType;
        this.rowUnmapper = resolveRowUnmapper(type);
        this.rowMapper = resolveRowMapper(type);
        this.sqlGenerator = sqlGenerator == null ? DEFAULT_GENERATOR : sqlGenerator;
        this.table = new TableDescription(table, idColumn);
    }

    public static <T> RowMapper<T> resolveRowMapper(Class<T> type){
        JdbcMarshallers a = AnnotationUtils.findAnnotation(type, JdbcMarshallers.class);
        if( a != null ){
            try {
                RowMapper instance = a.mapper().newInstance();
                if( instance instanceof BeanPropertyRowMapper){
                    ((BeanPropertyRowMapper) instance).setMappedClass(type);
                }
                return instance;
            } catch (Exception e){
                throw new RuntimeException("Failed instantiating RowMapper for " + type.getName(), e);
            }
        }

        if( PRIMITIVES.contains(type) ){
            return new SingleColumnRowMapper<>(type);
        }

        return new ColumnAwareBeanPropertyRowMapper<>(type);
    }

    private <T> RowUnmapper<T> resolveRowUnmapper(Class<T> type){
        JdbcMarshallers a = AnnotationUtils.findAnnotation(type, JdbcMarshallers.class);
        if( a != null ){
            try {
                RowUnmapper instance = a.unmapper().newInstance();
                return instance;
            } catch (Exception e){
                throw new RuntimeException("Failed instantiating RowUnmapper for " + type.getName(), e);
            }
        }

        return new MissingRowUnmapper<>();
    }

    private <T extends Persistable<ID>, ID extends Serializable> String resolveTableName(Class<T> type) {
        JdbcEntity a = type.getAnnotation(JdbcEntity.class);
        if( a != null && StringUtils.hasText(a.table()) ){
            return a.table();
        }
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getSimpleName());
    }

    /**
     * General purpose hook method that is called every time {@link #create} is called with a new entity.
     * <p/>
     * OVerride this method e.g. if you want to fetch auto-generated key from database
     *
     *
     * @param entity Entity that was passed to {@link #create}
     * @param generatedId ID generated during INSERT or NULL if not available/not generated.
     * todo: Type should be ID, not Number
     * @return Either the same object as an argument or completely different one
     */
    protected <S extends T> S postCreate(S entity, Number generatedId) {
        if( idType == Long.class ){
            entity.setId( (ID) (Long) generatedId.longValue());
        }else{
            log.warn("Unhandled generated ID type: {}", idType);
        }
        // TODO add others here...
        return entity;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        obtainJdbcTemplate();
        if (sqlGenerator == null) {
            obtainSqlGenerator();
        }

        applicationContext.getAutowireCapableBeanFactory().autowireBean(rowUnmapper);
    }

    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
    }

    protected TableDescription getTable() {
        return table;
    }

    private void obtainSqlGenerator() {
        try {
            sqlGenerator = applicationContext.getBean(SqlGenerator.class);
        } catch (NoSuchBeanDefinitionException e) {
            sqlGenerator = DEFAULT_GENERATOR;
        }
    }

    private void obtainJdbcTemplate() {
        try {
            jdbcOperations = applicationContext.getBean(JdbcOperations.class);
        } catch (NoSuchBeanDefinitionException e) {
            final DataSource dataSource = applicationContext.getBean(DataSource.class);
            jdbcOperations = new JdbcTemplate(dataSource);
        }
    }

    @Override
    public long count() {
        return jdbcOperations.queryForObject(sqlGenerator.count(table), Long.class);
    }

    @Override
    public void delete(ID id) {
        jdbcOperations.update(sqlGenerator.deleteById(table), idToObjectArray(id));
    }

    @Override
    public void delete(T entity) {
        jdbcOperations.update(sqlGenerator.deleteById(table), idToObjectArray(entity.getId()));
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (T t : entities) {
            delete(t);
        }
    }

    @Override
    public void deleteAll() {
        jdbcOperations.update(sqlGenerator.deleteAll(table));
    }

    @Override
    public boolean exists(ID id) {
        return jdbcOperations.queryForObject(sqlGenerator.countById(table), Integer.class, idToObjectArray(id)) > 0;
    }

    @Override
    public List<T> findAll() {
        return jdbcOperations.query(sqlGenerator.selectAll(table), rowMapper);
    }

    @Override
    public T findOne(ID id) {
        final Object[] idColumns = idToObjectArray(id);
        final List<T> entityOrEmpty = jdbcOperations.query(sqlGenerator.selectById(table), idColumns, rowMapper);
        return entityOrEmpty.isEmpty() ? null : entityOrEmpty.get(0);
    }

    private static <ID> Object[] idToObjectArray(ID id) {
        if (id instanceof Object[])
            return (Object[]) id;
        else
            return new Object[]{id};
    }

    private static <ID> List<Object> idToObjectList(ID id) {
        if (id instanceof Object[])
            return Arrays.asList((Object[]) id);
        else
            return Collections.<Object>singletonList(id);
    }

    @Override
    public <S extends T> S save(S entity) {
        if (entity.isNew()) {
            return create(entity);
        } else {
            return update(entity);
        }
    }

    protected <S extends T> S update(S entity) {
        final Map<String, Object> columns = preUpdate(entity, columns(entity));
        final List<Object> idValues = removeIdColumns(columns);
        final String updateQuery = sqlGenerator.update(table, columns);
        for (int i = 0; i < table.getIdColumns().size(); ++i) {
            columns.put(table.getIdColumns().get(i), idValues.get(i));
        }
        final Object[] queryParams = columns.values().toArray();
        jdbcOperations.update(updateQuery, queryParams);
        return postUpdate(entity);
    }

    protected Map<String,Object> preUpdate(T entity, Map<String, Object> columns) {
        return columns;
    }

    protected <S extends T> S create(S entity) {
        final Map<String, Object> columns = preCreate(columns(entity), entity);
        if (entity.getId() == null) {
            return createWithAutoGeneratedKey(entity, columns);
        } else {
            return createWithManuallyAssignedKey(entity, columns);
        }
    }

    private <S extends T> S createWithManuallyAssignedKey(S entity, Map<String, Object> columns) {
        final String createQuery = sqlGenerator.create(table, columns);
        final Object[] queryParams = columns.values().toArray();
        jdbcOperations.update(createQuery, queryParams);
        return postCreate(entity, null);
    }

    private <S extends T> S createWithAutoGeneratedKey(S entity, Map<String, Object> columns) {
        removeIdColumns(columns);
        final String createQuery = sqlGenerator.create(table, columns);
        final Object[] queryParams = columns.values().toArray();
        final GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbcOperations.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                final String idColumnName = table.getIdColumns().get(0);
                final PreparedStatement ps = con.prepareStatement(createQuery, new String[]{idColumnName});
                for (int i = 0; i < queryParams.length; ++i) {
                    ps.setObject(i + 1, queryParams[i]);
                }
                return ps;
            }
        }, key);
        return postCreate(entity, key.getKey());
    }

    private List<Object> removeIdColumns(Map<String, Object> columns) {
        List<Object> idColumnsValues = new ArrayList<Object>(columns.size());
        for (String idColumn : table.getIdColumns()) {
            idColumnsValues.add(columns.remove(idColumn));
        }
        return idColumnsValues;
    }

    protected Map<String, Object> preCreate(Map<String, Object> columns, T entity) {
        return columns;
    }

    private LinkedHashMap<String, Object> columns(T entity) {
        return new LinkedHashMap<String, Object>(rowUnmapper.mapColumns(entity));
    }

    protected <S extends T> S postUpdate(S entity) {
        return entity;
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        List<S> ret = new ArrayList<S>();
        for (S s : entities) {
            ret.add(save(s));
        }
        return ret;
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        final List<ID> idsList = toList(ids);
        if (idsList.isEmpty()) {
            return Collections.emptyList();
        }
        final Object[] idColumnValues = flatten(idsList);
        return jdbcOperations.query(sqlGenerator.selectByIds(table, idsList.size()), rowMapper, idColumnValues);
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        final List<T> result = new ArrayList<T>();
        for (T item : iterable) {
            result.add(item);
        }
        return result;
    }

    private static <ID> Object[] flatten(List<ID> ids) {
        final List<Object> result = new ArrayList<Object>();
        for (ID id : ids) {
            result.addAll(idToObjectList(id));
        }
        return result.toArray();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return jdbcOperations.query(sqlGenerator.selectAll(table, sort), rowMapper);
    }

    @Override
    public Page<T> findAll(Pageable page) {
        String query = sqlGenerator.selectAll(table, page);
        return new PageImpl<T>(jdbcOperations.query(query, rowMapper), page, count());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
