package other;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Helper class which is able to autowire a specified class. It holds a static reference to the {@link org
 * .springframework.context.ApplicationContext}.
 *
 * See http://guylabs.ch/2014/02/22/autowiring-pring-beans-in-hibernate-jpa-entity-listeners/
 */
public final class AutowireUtil implements ApplicationContextAware {

    private static final AutowireUtil INSTANCE = new AutowireUtil();
    private static ApplicationContext applicationContext;

    public static void autowire(Object classToAutowire) {
        applicationContext.getAutowireCapableBeanFactory()
                .autowireBean(classToAutowire);

    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        AutowireUtil.applicationContext = applicationContext;
    }

    /**
     * @return the singleton instance.
     */
    public static AutowireUtil getInstance() {
        return INSTANCE;
    }

}
