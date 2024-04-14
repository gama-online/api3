package lt.gama.auth.annotation;

import lt.gama.model.type.enums.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gama-online
 * Created by valdas on 2016-03-09.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresPermissions {

     Permission[] value() default {};
}
