package org.imanity.framework.bukkit.test;

import org.bukkit.Material;
import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

    /**
     * @return The name of the test
     */
    String value();

    @Language("SpEL") String condition() default "";

    Material display() default Material.SIGN;

    boolean requireExecutor() default false;

    int order() default -1;

}
