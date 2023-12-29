package vn.com.twendie.avis.api.core.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.annotation.Hidden;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;

@Component
public class ReflectUtils {

    private final StrUtils strUtils;

    public ReflectUtils(StrUtils strUtils) {
        this.strUtils = strUtils;
    }

    public List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.forEach(field -> field.setAccessible(true));
        Class<?> parent = clazz.getSuperclass();
        if (Objects.nonNull(parent)) {
            fields.addAll(getAllFields(parent));
        }
        return fields;
    }

    public List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods;
        if (Proxy.isProxyClass(clazz)) {
            methods = Arrays.stream(clazz.getInterfaces())
                    .map(this::getAllMethods)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            methods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
            Class<?> parent = clazz.getSuperclass();
            if (Objects.nonNull(parent)) {
                methods.addAll(getAllMethods(parent));
            }
        }
        methods.forEach(method -> method.setAccessible(true));
        return methods;
    }

    public Field getFieldByName(Class<?> clazz, String fieldName) {
        return getAllFields(clazz).stream()
                .filter(field -> field.getName().equals(strUtils.toCamelCase(fieldName)))
                .findFirst()
                .orElse(null);
    }

    public Field getFieldByJsonProperty(Class<?> clazz, String propertyName) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(JsonProperty.class))
                .filter(field -> field.getAnnotation(JsonProperty.class).value().equals(propertyName))
                .findFirst()
                .orElse(null);
    }

    public Field getFieldByColumn(Class<?> clazz, String columnName) {
        return getAllFields(clazz).stream()
                .filter(field -> {
                    if (field.isAnnotationPresent(Column.class)) {
                        return field.getAnnotation(Column.class).name().equals(columnName);
                    } else if (field.isAnnotationPresent(JoinColumn.class)) {
                        return field.getAnnotation(JoinColumn.class).name().equals(columnName);
                    } else {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }

    public List<Field> getFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    public Field getField(Class<?> clazz, String name) {
        Field field = getFieldByName(clazz, name);
        if (Objects.isNull(field)) {
            field = getFieldByJsonProperty(clazz, name);
        }
        if (Objects.isNull(field)) {
            field = getFieldByColumn(clazz, name);
        }
        return field;
    }

    public Method getMethodByName(Class<?> clazz, String methodName) {
        return getAllMethods(clazz).stream()
                .filter(method -> method.getName().equals(strUtils.toCamelCase(methodName)) &&
                        method.getParameterCount() == 0)
                .findFirst()
                .orElse(null);
    }

    public Method getGetterMethod(Class<?> clazz, String fieldName) {
        return getMethodByName(clazz, "get" + strUtils.toCaseFormat(fieldName, UPPER_CAMEL));
    }

    public Method getMethodByJsonProperty(Class<?> clazz, String propertyName) {
        return getAllMethods(clazz).stream()
                .filter(method -> method.isAnnotationPresent(JsonProperty.class))
                .filter(method -> method.getAnnotation(JsonProperty.class).value().equals(propertyName))
                .findFirst()
                .orElse(null);
    }

    public Method getMethodByColumn(Class<?> clazz, String columnName) {
        return getAllMethods(clazz).stream()
                .filter(method -> {
                    if (method.isAnnotationPresent(Column.class)) {
                        return method.getAnnotation(Column.class).name().equals(columnName);
                    } else if (method.isAnnotationPresent(JoinColumn.class)) {
                        return method.getAnnotation(JoinColumn.class).name().equals(columnName);
                    } else {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }

    public Method getMethod(Class<?> clazz, String name) {
        Method method = getMethodByName(clazz, name);
        if (Objects.isNull(method)) {
            method = getGetterMethod(clazz, name);
        }
        if (Objects.isNull(method)) {
            method = getMethodByJsonProperty(clazz, name);
        }
        if (Objects.isNull(method)) {
            method = getMethodByColumn(clazz, name);
        }
        return method;
    }

    public Object getValueByFieldName(Object object, String fieldName) throws ReflectiveOperationException {
        Field field = getFieldByName(object.getClass(), fieldName);
        if (Objects.nonNull(field)) {
            return field.get(object);
        } else {
            throw new NoSuchFieldException("Not found field: " + fieldName +
                    " in class: " + object.getClass());
        }
    }

    public Object getValueByMethodName(Object object, String methodName) throws ReflectiveOperationException {
        Method method = getMethodByName(object.getClass(), methodName);
        if (Objects.nonNull(method)) {
            return method.invoke(object);
        } else {
            throw new NoSuchMethodException("Not found method: " + methodName +
                    " in class " + object.getClass());
        }
    }

    public Object getValueByJsonProperty(Object object, String propertyName) throws ReflectiveOperationException {
        Field field = getFieldByJsonProperty(object.getClass(), propertyName);
        if (Objects.nonNull(field)) {
            return field.get(object);
        }
        Method method = getMethodByJsonProperty(object.getClass(), propertyName);
        if (Objects.nonNull(method)) {
            return method.invoke(object);
        }
        throw new ReflectiveOperationException("Not found json property: " + propertyName +
                " in class: " + object.getClass());
    }

    public Object getValueByColumn(Object object, String columnName) throws ReflectiveOperationException {
        Field field = getFieldByColumn(object.getClass(), columnName);
        if (Objects.nonNull(field)) {
            return field.get(object);
        }
        Method method = getMethodByColumn(object.getClass(), columnName);
        if (Objects.nonNull(method)) {
            return method.invoke(object);
        }
        throw new ReflectiveOperationException("Not found column: " + columnName +
                " in class: " + object.getClass());
    }

    public Object getValue(Object object, String name) throws ReflectiveOperationException {
        Field field = getField(object.getClass(), name);
        if (Objects.nonNull(field)) {
            return field.get(object);
        }
        Method method = getMethod(object.getClass(), name);
        if (Objects.nonNull(method)) {
            return method.invoke(object);
        }
        throw new ReflectiveOperationException("Not found field or method: " + name +
                " in class: " + object.getClass());
    }

    public void hideInfo(Object object) {
        getFieldsByAnnotation(object.getClass(), Hidden.class)
                .forEach(field -> {
                    try {
                        field.set(object, null);
                    } catch (IllegalAccessException ignored) {
                    }
                });
    }

}
