package io.aiur.oss.db.jdbc.jdbc.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * Created by kalebscholes on 5/12/16.
 */
public interface JdbcPersistentEntity<T> extends PersistentEntity<T, JdbcPersistentProperty> {
}
