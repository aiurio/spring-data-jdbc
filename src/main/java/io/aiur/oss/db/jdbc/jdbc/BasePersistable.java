package io.aiur.oss.db.jdbc.jdbc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * Created by dave on 12/15/15.
 */
public interface BasePersistable<ID extends Serializable> extends Persistable<ID> {

    void setId(ID id);

    @JsonIgnore
    default boolean isNew(){
        return getId() == null;
    }

}
