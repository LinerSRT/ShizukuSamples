package ru.liner.shizukusamples.shizuku;

import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;
import ru.liner.shizukusamples.utils.Consumer;

/**
 * Author: Line'R
 * E-mail: serinity320@mail.com
 * Github: https://github.com/LinerSRT
 * Date: 17.09.2023, 18:37
 *
 * @noinspection JavadocLinkAsPlainText, unchecked
 */
public class ShizukuService<Service extends IInterface> implements Shizuku.OnBinderDeadListener, Shizuku.OnBinderReceivedListener, Shizuku.OnRequestPermissionResultListener {
    private static final int REQUEST_CODE_SHIZUKU = 9988;
    @NonNull
    private final Class<Service> serviceClass;
    private Callback<Service> callback;
    @ShizukuState
    private int shizukuState;

    public static <Service extends IInterface> ShizukuService<Service> of(@NonNull Class<Service> serviceClass) {
        return new ShizukuService<>(serviceClass);
    }

    private ShizukuService(@NonNull Class<Service> serviceClass) {
        this.serviceClass = serviceClass;
        this.shizukuState = ShizukuState.UNKNOWN;
    }

    public ShizukuService<Service> checkShizuku(@NonNull Callback<Service> callback) {
        this.callback = callback;
        this.shizukuState = obtainShizukuState();
        if(shizukuState == ShizukuState.NORMAL)
            callback.whenReady(this);
        return this;
    }

    public Service load(@NonNull String serviceName) {
        return Consumer.of(
                        Arrays.stream(serviceClass.getDeclaredClasses())
                                .filter(aClass -> aClass.getSimpleName().equals("Stub"))
                                .findFirst().orElse(null)
                )
                .next((Consumer.FunctionB<Class<?>, Method>) input -> {
                    try {
                        return input.getDeclaredMethod("asInterface", IBinder.class);
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                })
                .next(input -> {
                    try {
                        input.setAccessible(true);
                        return (Service) input.invoke(serviceClass, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName)));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                }).get();
    }
    @Override
    public void onBinderDead() {
        shizukuState = ShizukuState.BINDER_DEAD;
        Shizuku.addBinderReceivedListener(this);
        callback.whenDied(this);
    }

    @Override
    public void onBinderReceived() {
        shizukuState = obtainShizukuState();
        Shizuku.removeBinderReceivedListener(this);
        if (shizukuState == ShizukuState.NORMAL)
            callback.whenReady(this);
    }

    @ShizukuState
    private int obtainShizukuState() {
        shizukuState = ShizukuState.UNKNOWN;
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                if (!Shizuku.shouldShowRequestPermissionRationale()) {
                    shizukuState = ShizukuState.PERMISSION_WAIT;
                    Shizuku.addRequestPermissionResultListener(this);
                    Shizuku.requestPermission(REQUEST_CODE_SHIZUKU);
                } else {
                    shizukuState = ShizukuState.PERMISSION_DENIED;
                }
            } else {
                shizukuState = ShizukuState.NORMAL;
            }
        } else {
            Shizuku.addBinderReceivedListener(this);
            shizukuState = ShizukuState.BINDER_DEAD;
        }
        return shizukuState;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == REQUEST_CODE_SHIZUKU && grantResult == PackageManager.PERMISSION_GRANTED) {
            shizukuState = ShizukuState.NORMAL;
            callback.whenReady(this);
        } else {
            shizukuState = ShizukuState.PERMISSION_DENIED;
            callback.whenPermissionDenied(this);
        }
        Shizuku.removeRequestPermissionResultListener(this);
    }

    public interface Callback<Service extends IInterface> {
        default void whenReady(@NonNull ShizukuService<Service> service){}

        default void whenDied(@NonNull ShizukuService<Service> service){}

        default void whenPermissionDenied(@NonNull ShizukuService<Service> service){}
    }
}
