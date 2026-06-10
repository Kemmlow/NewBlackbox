package top.niunaijun.blackbox.fake.service;

import android.content.pm.PackageManager;
import java.lang.reflect.Method;
import android.os.IBinder;

import black.android.app.BRActivityThread;
import black.android.os.BRServiceManager;
import black.android.permission.BRIPermissionManagerStub;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;
import top.niunaijun.blackbox.utils.Slog;

public class IPermissionManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IPermissionManagerProxy";
    private static final String P = "permissionmgr";

    public IPermissionManagerProxy() {
        super(BRServiceManager.get().getService(P));
    }

    @Override
    protected Object getWho() {
        IBinder binder = BRServiceManager.get().getService(P);
        return BRIPermissionManagerStub.get().asInterface(binder);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(P);
        try {
            BRActivityThread.getWithException()._set_sPermissionManager(proxyInvocation);
        } catch (Exception ignored) {}
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
}
