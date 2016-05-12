package io.aiur.oss.db.jdbc.jdbc.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

/**
 * Created by kalebscholes on 5/12/16.
 */
public class JdbcPersistentEntityImpl<T> extends BasicPersistentEntity<T, JdbcPersistentProperty> implements JdbcPersistentEntity<T>{

    public JdbcPersistentEntityImpl(TypeInformation<T> information) {
        super(information, null);
    }

    public JdbcPersistentEntityImpl(TypeInformation<T> information, Comparator<JdbcPersistentProperty> comparator) {
        super(information, comparator);
    }
}
