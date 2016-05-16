package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.util.Comparator;

/**
 * Created by kalebscholes on 5/12/16.
 */
@Slf4j
public class JdbcPersistentEntityImpl<T> extends BasicPersistentEntity<T, JdbcPersistentProperty> implements JdbcPersistentEntity<T>{

    public JdbcPersistentEntityImpl(TypeInformation<T> information) {
        this(information, null);
    }

    public JdbcPersistentEntityImpl(TypeInformation<T> information, Comparator<JdbcPersistentProperty> comparator) {
        super(information, comparator);
        JdbcPersistentEntityImpl<T> basic = this;
        Class<?> domainType = information.getType();

        ReflectionUtils.doWithFields(domainType, (field)->{
        if( basic.hasIdProperty() ){
            log.warn("Found multiple ID properties for {}: {} and {}",
                        domainType, basic.getIdProperty().getField().getType(), field.getType());
        }else{
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), domainType);
                SimpleTypeHolder sth = new SimpleTypeHolder(Sets.newHashSet(field.getType()), true);
                JdbcPersistentProperty prop = new JdbcPersistentPropertyImpl(field, pd, basic, sth);
                basic.addPersistentProperty(prop);
            }catch(Exception e){
                log.warn("Failed adding PersistentProperty ID ", e);
            }
        }
        }, (matchField) -> matchField.getDeclaredAnnotation(Id.class) != null);
        
    }
}
