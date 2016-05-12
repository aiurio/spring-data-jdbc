
package io.aiur.oss.db.jdbc.jdbc.binding;

import io.aiur.oss.db.jdbc.jdbc.annotation.EnableJdbcRepositories;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcEntity;
import io.aiur.oss.db.jdbc.jdbc.audit.JdbcAuditingHandler;
import io.aiur.oss.db.jdbc.jdbc.mapping.JdbcPersistentEntityImpl;
import io.aiur.oss.db.jdbc.jdbc.mapping.JdbcPersistentProperty;
import io.aiur.oss.db.jdbc.jdbc.mapping.JdbcPersistentPropertyImpl;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class JdbcRepositoryRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    public JdbcRepositoryRegistrar(){}

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableJdbcRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableMyRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new Config();
    }

    @EnableJdbcRepositories
    private static class EnableMyRepositoriesConfiguration {}

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        super.registerBeanDefinitions(importingClassMetadata, registry);

        Reflections reflections = new Reflections( getBasePackages() );
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(JdbcEntity.class);

        registry.registerBeanDefinition("mappingDefinition", mappingDefinition(entities));

        registry.registerBeanDefinition("jdbcEventFilter", new RootBeanDefinition(JdbcEventFilter.class));

        Map<String, Object> attr = importingClassMetadata
                .getAnnotationAttributes(EnableJdbcRepositories.class.getName());
        boolean autoGenerate = (Boolean) attr.get("generateRepositories");
        if( autoGenerate ) {
            for (Class<?> entity : entities) {
                registry.registerBeanDefinition(entity.getSimpleName() + "Repository", repoDefinition(entity));
            }
        }

        boolean enableAuditing = (Boolean) attr.get("enableAuditing");
        if( enableAuditing ){
            registry.registerBeanDefinition("jdbcEventAuditor", new RootBeanDefinition(JdbcAuditingHandler.class));
        }
    }

    public BeanDefinition repoDefinition(Class<?> domainType){
        return BeanDefinitionBuilder.rootBeanDefinition(JdbcRepositoryFactory.class)
                .addPropertyValue("idType", Long.class)
                .addPropertyValue("domainType", domainType)
                .addPropertyReference("jdbcOperations", "jdbcTemplate")
                .addPropertyValue("repositoryInterface", PagingAndSortingRepository.class)
                .getBeanDefinition();
    }

    public BeanDefinition mappingDefinition(Set<Class<?>> entities){
        return BeanDefinitionBuilder.rootBeanDefinition(JdbcMappingContext.class)
                .addPropertyValue("initialEntitySet", entities)
                .getBeanDefinition();
    }

    public static class JdbcMappingContext extends AbstractMappingContext<JdbcPersistentEntityImpl<?>, JdbcPersistentProperty> {
        @Override
        protected <T> JdbcPersistentEntityImpl<?> createPersistentEntity(TypeInformation<T> typeInformation) {
            TypeInformation<T> typeInfo = ClassTypeInformation.from(typeInformation.getType());
            return new JdbcPersistentEntityImpl<>(typeInfo);
        }

        @Override
        protected JdbcPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, JdbcPersistentEntityImpl<?> owner, SimpleTypeHolder simpleTypeHolder) {
            return new JdbcPersistentPropertyImpl(field, descriptor, owner, simpleTypeHolder);
        }
    }

    public static class Config extends RepositoryConfigurationExtensionSupport {

        @Override
        protected String getModulePrefix() {
            return "jdbc";
        }

        @Override
        public String getRepositoryFactoryClassName() {
            return JdbcRepositoryFactory.class.getName();
        }
    }
}
