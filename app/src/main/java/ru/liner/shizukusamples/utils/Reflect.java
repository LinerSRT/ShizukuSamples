package ru.liner.shizukusamples.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.Arrays;

/**
 * Author: Line'R
 * E-mail: serinity320@mail.com
 * Github: https://github.com/LinerSRT
 * Date: 17.09.2023, 18:40
 * @noinspection unchecked
 */
public class Reflect {
    private static final String TAG = Reflect.class.getSimpleName();
    private Class<?> objectClass;
    private Object object;

    public Reflect(@NonNull Object object) {
        this.objectClass = object.getClass();
        this.object = object;
    }

    @Nullable
    private <Result> Result invoke(@NonNull Method method, @NonNull Object... objects){
        if(!method.isAccessible())
            method.setAccessible(true);
        try {
            Log.d(TAG, String.format("Invoked %s#%s with {%s}", objectClass.getSimpleName(), method.getName(), Arrays.toString(objects)));
            return (Result) method.invoke(object, objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, String.format("Cannot invoke [%s] from {%s}. Reason: %s", method.getName(), objectClass.getSimpleName(), e.getMessage()));
            return null;
        }
    }

    @Nullable
    public <Result> Result invoke(@NonNull String methodName, @NonNull Object... objects){
        try {
            return invoke(objectClass.getDeclaredMethod(methodName, parametersToTypes(objects)), objects);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, String.format("Cannot find %s#%s", objectClass.getSimpleName(), methodName));
            for(Method method : objectClass.getDeclaredMethods())
                if(method.getName().equals(methodName)){
                    try {
                        return invoke(objectClass.getDeclaredMethod(methodName, parametersToTypes(objects)), objects);
                    } catch (NoSuchMethodException ex) {
                        Log.e(TAG, String.format("Cannot find %s#%s", objectClass.getSimpleName(), methodName));
                        return null;
                    }
                }
        }
        return null;
    }


    private Class<?>[] parametersToTypes(@NonNull Object... objects){
        Class<?>[] types = new Class[objects.length];
        for (int i = 0; i < objects.length; i++)
            types[i] = objects[i].getClass();
        return types;
    }
}
