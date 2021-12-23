package com.golfing8.elevatorsigns.config.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {
    String path() default "";
    
    String name() default "";
}
 