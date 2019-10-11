package com.atguigu.gmall.index.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GuliCache {

    @AliasFor("prefix")
    String value() default "";

    @AliasFor("value")
    String prefix() default "";
}
