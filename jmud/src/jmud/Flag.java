package jmud;

import java.lang.annotation.*;

/**
 * Author: Chris Maguire
 * Date: 24-Dec-2007
 * Time: 6:46:03 PM
 */
@java.lang.annotation.Target({ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Flag {
    String name() default "";

    String[] aliases() default {};
}
