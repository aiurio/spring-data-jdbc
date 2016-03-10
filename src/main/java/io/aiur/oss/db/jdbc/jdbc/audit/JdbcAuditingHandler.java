package io.aiur.oss.db.jdbc.jdbc.audit;

import org.springframework.context.ApplicationListener;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
import org.springframework.data.rest.core.event.RepositoryEvent;

import javax.inject.Inject;

public class JdbcAuditingHandler extends AuditingHandler implements ApplicationListener<RepositoryEvent> {

    @Inject
    public JdbcAuditingHandler(PersistentEntities entities) {
        super(entities);
    }

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        if( BeforeCreateEvent.class.isInstance(event) || BeforeSaveEvent.class.isInstance(event) ){
            Object source = event.getSource();
            if( isNew(source) ){
                markCreated(source);
            }else{
                markModified(source);
            }
        }
    }

    private boolean isNew(Object source) {
        // TODO what other cases do we need to cover?
        return source != null
                && source instanceof Persistable
                && ((Persistable) source).isNew();
    }
}
