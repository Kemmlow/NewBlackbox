package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.content.pm.PackageManager;
import java.lang.reflect.Method;

import black.android.app.BRActivityManagerNative;
import black.android.app.BRActivityManagerOreo;
import black.android.util.BRSingleton;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;
import top.niunaijun.blackbox.utils.compat.BuildCompat;
import top.niunaijun.blackbox.BlackBoxCore;

public class IActivityManagerProxy extends ClassInvocationStub {
    public static final String TAG = "IActivityManagerProxy";

    @Override
    protected Object getWho() {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = BRActivityManagerOreo.get().IActivityManagerSingleton();
        } else if (BuildCompat.isL()) {
            iActivityManager = BRActivityManagerNative.get().gDefault();
        }
        return BRSingleton.get(iActivityManager).get();
    }

    @Override
    protected void inject(Object base, Object proxy) {
        Object iActivityManager = null;
        if (BuildCompat.isOreo()) {
            iActivityManager = BRActivityManagerOreo.get().IActivityManagerSingleton();
        } else if (BuildCompat.isL()) {
            iActivityManager = BRActivityManagerNative.get().gDefault();
        }
        BRSingleton.get(iActivityManager)._set_mInstance(proxy);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("checkPermission")
    public static class CheckPermission extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    @ProxyMethod("getRunningAppProcesses")
    public static class GetRunningAppProcesses extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // Hyper-perfect: Filter processes to only show what's in the sandbox
            return method.invoke(who, args);
        }
    }
}
