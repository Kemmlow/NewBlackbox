package top.niunaijun.blackbox.fake.service;

import android.content.pm.PackageManager;
import java.lang.reflect.Method;
import android.os.IBinder;

import black.android.os.BRServiceManager;
import black.android.content.pm.BRIPackageManagerStub;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;
import top.niunaijun.blackbox.utils.Slog;

public class IPackageManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IPackageManagerProxy";

    public IPackageManagerProxy() {
        super(BRServiceManager.get().getService("package"));
    }

    @Override
    protected Object getWho() {
        IBinder binder = BRServiceManager.get().getService("package");
        return BRIPackageManagerStub.get().asInterface(binder);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("package");
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
