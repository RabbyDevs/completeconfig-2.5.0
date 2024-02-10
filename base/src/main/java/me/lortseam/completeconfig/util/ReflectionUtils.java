package me.lortseam.completeconfig.util;

import com.google.common.reflect.TypeToken;
import io.leangen.geantyref.GenericTypeReflector;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.Optional;

@UtilityClass
public final class ReflectionUtils {

    public static Class<?> getTypeClass(Type type) {
        return TypeToken.of(type).getRawType();
    }

    public static Type getFieldType(Field field) {
        return GenericTypeReflector.getExactFieldType(field, field.getDeclaringClass());
    }

    public static Type[] getFieldGenericTypes(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        }
        return new Type[0];
    }

    public static <T> T instantiateClass(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        if (!constructor.canAccess(null)) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance();
    }

    public static Optional<Method> getSetterMethod(Field field, Object object) {
        Method method;
        try {
            method = field.getDeclaringClass().getDeclaredMethod("set" + StringUtils.capitalize(field.getName()), getTypeClass(getFieldType(field)));
        } catch (NoSuchMethodException ignore) {
            return Optional.empty();
        }
        if (Modifier.isStatic(field.getModifiers()) != Modifier.isStatic(method.getModifiers()) || !method.getReturnType().equals(Void.TYPE)) {
            return Optional.empty();
        }
        if (!method.canAccess(object)) {
            method.setAccessible(true);
        }
        return Optional.of(method);
    }

    public static Type boxType(Type type) {
        return GenericTypeReflector.box(type);
    }

}
