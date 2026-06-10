package edu.cnan.beehive.spring.base;

import java.lang.annotation.*;

/**
 * Marks a thread pool {@code @Bean} as eligible for dynamic management
 * by the Beehive framework.
 *
 * <p>This annotation carries no configuration — it serves purely as
 * a marker, analogous to {@link java.lang.Override}.
 *
 * @author cnan
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {
}
