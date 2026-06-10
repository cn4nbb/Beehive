package edu.cnan.beehive.spring.base.enable;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/**
 * Enables the Beehive dynamic thread-pool framework.
 *
 * <p>Usage:
 * <pre>{@code
 * @SpringBootApplication
 * @EnableBeehive
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * @author cnan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MarkerConfiguration.class)
@Documented
public @interface EnableBeehive {
}
