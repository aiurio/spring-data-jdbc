package io.aiur.oss.db.jdbc.jdbc;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * Created by dave on 12/16/15.
 */
public interface JdbcRepository<T extends BasePersistable<ID>, ID extends Serializable>
        extends PagingAndSortingRepository<T, ID> {

}
