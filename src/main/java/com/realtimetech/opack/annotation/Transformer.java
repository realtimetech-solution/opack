package com.realtimetech.opack.annotation;

import com.realtimetech.opack.transformer.TransformerInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
})
public @interface Transformer {
    Class<? extends TransformerInterface> transformer();
}