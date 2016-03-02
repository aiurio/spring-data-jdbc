
package io.aiur.oss.db.jdbc.jdbc.binding;

import io.aiur.oss.db.jdbc.jdbc.annotation.EnableJdbcRepositories;
import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcEntity;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.MutablePersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

        boolean autoGenerate = (Boolean) importingClassMetadata
                .getAnnotationAttributes(EnableJdbcRepositories.class.getName())
                .get("generateRepositories");

        if( autoGenerate ) {
            for (Class<?> entity : entities) {
                registry.registerBeanDefinition(entity.getSimpleName() + "Repository", repoDefinition(entity));
            }
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

    public static class JdbcMappingContext extends AbstractMappingContext {

        @Override
        protected MutablePersistentEntity createPersistentEntity(TypeInformation typeInformation) {
            TypeInformation<?> typeInfo = ClassTypeInformation.from(typeInformation.getType());
            return new BasicPersistentEntity(typeInfo);
        }

        @Override
        protected PersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, MutablePersistentEntity owner, SimpleTypeHolder simpleTypeHolder) {
            return new AnnotationBasedPersistentProperty(field, descriptor, owner, simpleTypeHolder){
                @Override
                protected Association createAssociation() {
                    return null;
                }
            };
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
