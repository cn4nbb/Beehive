package edu.cnan.beehive.spring.base.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Static holder for the Spring {@link ApplicationContext}.
 *
 * <p>Provides global access to the container from non-Spring-aware
 * code.  The context is injected by Spring via
 * {@link ApplicationContextAware} during bootstrap.
 *
 * @author cnan
 */
public class ApplicationContextHolder implements ApplicationContextAware {
    /** The singleton application context. */
    private static ApplicationContext CONTEXT;

    /**
     * {@inheritDoc}
     *
     * <p>Stores the context statically, so it can be retrieved
     * from non-Spring-managed classes.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    /** Shorthand for {@link ApplicationContext#getBean(Class)}. */
    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }

    /** Shorthand for {@link ApplicationContext#getBean(String, Class)}. */
    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT.getBean(name, clazz);
    }

    /** Shorthand for {@link ApplicationContext#getBeansOfType(Class)}. */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT.getBeansOfType(clazz);
    }

    /** Shorthand for {@link ApplicationContext#findAnnotationOnBean(String, Class)}. */
    public static <A extends Annotation> A findAnnotationOnBean(String name, Class<A> annotationType) {
        return CONTEXT.findAnnotationOnBean(name, annotationType);
    }

    /** Shorthand for {@link ApplicationContext#publishEvent(ApplicationEvent)}. */
    public static void publishEvent(ApplicationEvent event) {
        CONTEXT.publishEvent(event);
    }

    /**
     * Returns the raw {@code ApplicationContext} instance.
     * Prefer the typed shortcut methods unless you need
     * direct access to the context.
     */
    public static ApplicationContext getInstance() {
        return  CONTEXT;
    }
}
