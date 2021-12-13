package com.realtimetech.opack.compile;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.annotation.Ignore;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.TransformerFactory;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InfoCompiler {
    final @NotNull Opacker opacker;

    final @NotNull TransformerFactory transformerFactory;

    final @NotNull HashMap<Class<?>, ClassInfo> classInfoMap;

    public InfoCompiler(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerFactory = new TransformerFactory(opacker);

        this.classInfoMap = new HashMap<>();
    }

    Transformer getTransformer(AnnotatedElement annotatedElement) throws CompileException {
        if (annotatedElement.isAnnotationPresent(Transform.class)) {
            Transform transform = annotatedElement.getAnnotation(Transform.class);
            Class<Transformer> transformerInterfaceClass = (Class<Transformer>) transform.transformer();

            try {
                return (Transformer) this.transformerFactory.get(transformerInterfaceClass);
            } catch (InstantiationException e) {
                throw new CompileException(e);
            }
        }

        return null;
    }

    Class<?> getExplicitType(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(ExplicitType.class)) {
            ExplicitType explicit = annotatedElement.getAnnotation(ExplicitType.class);
            return explicit.type();
        }

        return null;
    }

    @NotNull ClassInfo compile(@NotNull Class<?> compileClass) throws CompileException {
        Field[] fields = ReflectionUtil.getAccessibleFields(compileClass);
        List<ClassInfo.FieldInfo> fieldInfos = new LinkedList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            Transformer transformer = this.getTransformer(field);
            Class<?> explicitType = this.getExplicitType(field);

            fieldInfos.add(new ClassInfo.FieldInfo(field, transformer, explicitType));
        }

        Transformer transformer = this.getTransformer(compileClass);

        return new ClassInfo(compileClass, transformer, fieldInfos.toArray(new ClassInfo.FieldInfo[fieldInfos.size()]));
    }

    public @NotNull ClassInfo get(@NotNull Class<?> targetClass) throws CompileException {
        if (!this.classInfoMap.containsKey(targetClass)) {
            synchronized (this.classInfoMap) {
                if (!this.classInfoMap.containsKey(targetClass)) {
                    ClassInfo classInfo = this.compile(targetClass);

                    this.classInfoMap.put(targetClass, classInfo);
                }
            }
        }

        return this.classInfoMap.get(targetClass);
    }
}
