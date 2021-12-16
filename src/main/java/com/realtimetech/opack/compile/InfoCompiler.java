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

    void addTransformer(List<Transformer> transformers, AnnotatedElement annotatedElement, boolean root) throws CompileException {
        if (annotatedElement instanceof Class) {
            Class<?> clazz = (Class<?>) annotatedElement;
            Class<?> superClass = clazz.getSuperclass();

            if (superClass != null && superClass != Object.class) {
                this.addTransformer(transformers, superClass, false);
            }

            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                this.addTransformer(transformers, interfaceClass, false);
            }
        }

        if (annotatedElement.isAnnotationPresent(Transform.class)) {
            Transform transform = annotatedElement.getAnnotation(Transform.class);

            if (root || transform.inheritable()) {
                Class<Transformer> transformerInterfaceClass = (Class<Transformer>) transform.transformer();

                try {
                    transformers.add(this.transformerFactory.get(transformerInterfaceClass));
                } catch (InstantiationException e) {
                    throw new CompileException(e);
                }
            }
        }
    }

    Transformer[] getTransformer(AnnotatedElement annotatedElement) throws CompileException {
        List<Transformer> transformers = new LinkedList<>();
        this.addTransformer(transformers, annotatedElement, true);
        return transformers.toArray(new Transformer[transformers.size()]);
    }

    Class<?> getExplicitType(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(ExplicitType.class)) {
            ExplicitType explicit = annotatedElement.getAnnotation(ExplicitType.class);
            return explicit.type();
        }

        return null;
    }

    @NotNull ClassInfo compile(@NotNull Class<?> compileClass) throws CompileException {
        List<ClassInfo.FieldInfo> fieldInfos = new LinkedList<>();
        Transformer[] transformers = new Transformer[0];

        if (!compileClass.isArray() &&
                compileClass != String.class &&
                !ReflectionUtil.isPrimitiveClass(compileClass) &&
                !ReflectionUtil.isWrapperClass(compileClass)) {

            Field[] fields = ReflectionUtil.getAccessibleFields(compileClass);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                Transformer[] fieldTransformers = this.getTransformer(field);
                Class<?> explicitType = this.getExplicitType(field);

                field.setAccessible(true);
                fieldInfos.add(new ClassInfo.FieldInfo(field, fieldTransformers.length > 0 ? fieldTransformers[0] : null, explicitType));
            }

            transformers = this.getTransformer(compileClass);
        }

        return new ClassInfo(compileClass, transformers, fieldInfos.toArray(new ClassInfo.FieldInfo[fieldInfos.size()]));
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
