package com.example.demo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Field;

public class AccessFieldNullifier {

    public static void nullifyAccessAnnotatedFields(Object object) {
        if (object == null) return;

        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);

                // Check if field has @Access annotation
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    // Nullify field value
                    JsonIgnore annotation = field.getAnnotation(JsonIgnore.class);
                    if (annotation.value()) {
                        field.set(object, null);
                    }
                }
                // If field is a nested object, recurse
                else if (value != null && !isPrimitiveOrWrapper(value.getClass())) {
                    nullifyAccessAnnotatedFields(value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                type.equals(Boolean.class) ||
                type.equals(Character.class);
    }
}
