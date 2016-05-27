package io.aiur.oss.db.jdbc.jdbc.mapping;

import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Created by kalebscholes on 5/12/16.
 */
public class JdbcPersistentPropertyImpl extends AnnotationBasedPersistentProperty<JdbcPersistentProperty> implements JdbcPersistentProperty {

    public JdbcPersistentPropertyImpl(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, JdbcPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    @Override
    protected Association<JdbcPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public boolean isIdProperty() {
        return isAnnotationPresent(Id.class);
    }
}
