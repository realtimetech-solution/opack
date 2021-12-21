package com.realtimetech.opack.annotation;

import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
})
public @interface   Transform {
    @NotNull Class<? extends Transformer> transformer();

    @NotNull boolean inheritable() default false;
}