package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.aiur.oss.db.jdbc.jdbc.convert.JdbcTypeConverter;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.RowUnmapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by dave on 12/16/15.
 */
public class ColumnAwareBeanPropertyRowUnmapper <T> implements RowUnmapper<T> {

    @Inject
    private List<JdbcTypeConverter> converters = Lists.newArrayList();

    @Override
    public Map<String, Object> mapColumns(T o) {
        ColumnAwareBeanPropertyRowMapper mapper = new ColumnAwareBeanPropertyRowMapper(o.getClass());

        Map<String, Object> result = Maps.newHashMap();

        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(o);
        List<ColumnAwareBeanPropertyRowMapper.ColumnPropertyMapping> fields = mapper.getMappings();
        fields.forEach((mapping) ->{
            Object value = bw.getPropertyValue(mapping.getProperty());
            Object origValue = value;
            Optional<JdbcTypeConverter> converter = converters.stream()
                    .filter(c -> c.canConvertToSqlType(origValue))
                    .sorted((a,b) -> a.getOrder() - b.getOrder() )
                    .findFirst();

            if( converter.isPresent() ){
                value = converter.get().convertToSqlType(value);
            }

            result.put(mapping.getColumn(), value );
        });

        return result;
    }
}