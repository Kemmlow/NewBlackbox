package top.niunaijun.blackbox.fake.service;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

import black.android.app.BRILocaleManagerStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;

public class ILocaleManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ILocaleManagerProxy";

    public ILocaleManagerProxy() {
        super(BRServiceManager.get().getService("locale"));
    }

    @Override
    protected Object getWho() {
        IBinder binder = BRServiceManager.get().getService("locale");
        if (binder != null) {
            return BRILocaleManagerStub.get().asInterface(binder);
        }
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("locale");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("setApplicationLocales")
    public static class SetApplicationLocales extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who != null) {
                    return method.invoke(who, args);
                }
                return null;
            } catch (Throwable e) {
                if (e.getCause() instanceof SecurityException) {
                    Slog.w(TAG, "Caught SecurityException in setApplicationLocales, swallowing to prevent crash: " + e.getCause().getMessage());
                    return null;
                }
                throw e;
            }
        }
    }
}
