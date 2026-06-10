package de.robv.android.xposed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class XposedHelpers {
    public static void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            XC_MethodHook callback = (XC_MethodHook) parameterTypesAndCallback[parameterTypesAndCallback.length - 1];
            Method method = clazz.getDeclaredMethod(methodName, getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));
            XposedBridge.hookMethod(method, callback);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    private static Class<?>[] getParameterClasses(ClassLoader classLoader, Object[] parameterTypesAndCallback) throws ClassNotFoundException {
        Class<?>[] classes = new Class<?>[parameterTypesAndCallback.length - 1];
        for (int i = 0; i < parameterTypesAndCallback.length - 1; i++) {
            Object type = parameterTypesAndCallback[i];
            if (type instanceof Class) classes[i] = (Class<?>) type;
            else classes[i] = classLoader.loadClass((String) type);
        }
        return classes;
    }
}
