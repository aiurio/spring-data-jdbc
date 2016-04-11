package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcColumn;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcColumnConvert;
import io.aiur.oss.db.jdbc.jdbc.convert.JodaDateTimeEditor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.data.annotation.Transient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 12/16/15.
 */
public class ColumnAwareBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    private List<ColumnPropertyMapping> mappings;

    /**
     * Changes enum values to not be 0-indexed, so your first enum correlate to 1 instead of 0
     */
    @Getter @Setter
    private boolean offsetEnumIndex = true;

    public ColumnAwareBeanPropertyRowMapper() {
        super();
    }

    public ColumnAwareBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public ColumnAwareBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
        super(mappedClass, checkFullyPopulated);
    }

    @Override
    protected void initBeanWrapper(BeanWrapper bw) {
        super.initBeanWrapper(bw);
        bw.registerCustomEditor(DateTime.class, new JodaDateTimeEditor());
    }

    @Override
    protected void initialize(Class<T> mappedClass) {
        super.initialize(mappedClass);
        mappings = Lists.newArrayList();

        Field mfFields = ReflectionUtils.findField(getClass(), "mappedFields", Map.class);
        ReflectionUtils.makeAccessible(mfFields);
        Map<String, PropertyDescriptor> mappedFields = (Map<String, PropertyDescriptor>) ReflectionUtils.getField(mfFields, this);

        Map<String, PropertyDescriptor> overrides = Maps.newHashMap();
        List<String> removals = Lists.newArrayList();

        mappedFields.forEach((f, pd) -> {
            Field field = ReflectionUtils.findField( pd.getReadMethod().getDeclaringClass(), pd.getName());
            JdbcColumn columnDef = field.getAnnotation(JdbcColumn.class);

            // look for a conversion rule on the field, or the type (if not on the field)
            JdbcColumnConvert convert = field.getAnnotation(JdbcColumnConvert.class);
            convert = convert == null ? field.getDeclaringClass().getAnnotation(JdbcColumnConvert.class) : convert;

            // default to lower_hyphen columns (if not specified)
            CaseFormat columnFormat = convert == null ? CaseFormat.LOWER_UNDERSCORE : convert.value();

            Transient trans = field.getAnnotation(Transient.class);
            if( trans != null || Modifier.isTransient(field.getModifiers())) {
                removals.add(f);
            }else if( columnDef != null ) {
                overrides.put(columnDef.value(), pd);
                removals.add(f);
                mappings.add(new ColumnPropertyMapping(pd.getName(), columnDef.value(), pd));
            }else{
                String columnName = CaseFormat.LOWER_CAMEL.to(columnFormat, pd.getName());
                mappings.add(new ColumnPropertyMapping(pd.getName(), columnName, pd));
            }
        });

        removals.forEach(mappedFields::remove);
        mappedFields.putAll(overrides);
    }

    public List<ColumnPropertyMapping> getMappings() {
        return mappings;
    }

    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        Object value = super.getColumnValue(rs, index, pd);
        if( value != null
                && pd.getPropertyType().isEnum()
                && Number.class.isAssignableFrom(value.getClass()) ){

            int ordinal = ((Number) value).intValue();

            if( offsetEnumIndex ){
                ordinal -= 1;
            }

            Object[] enums = pd.getPropertyType().getEnumConstants();
            value = enums[ordinal];
        }

        return value;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ColumnPropertyMapping {
        private final String property, column;
        private final PropertyDescriptor descriptor;
    }

}
