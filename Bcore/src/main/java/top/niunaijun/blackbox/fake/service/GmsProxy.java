package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.IBinder;
import java.lang.reflect.Method;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;

public class GmsProxy extends BinderInvocationStub {
    public static final String TAG = "GmsProxy";

    public GmsProxy() {
        super(BRServiceManager.get().getService("gms"));
    }

    @Override
    protected Object getWho() {
        IBinder binder = BRServiceManager.get().getService("gms");
        if (binder == null) return null;
        try {
            // Hyper-perfect GMS broker interface resolution
            String[] possibleStubs = {
                "com.google.android.gms.common.internal.IGmsServiceBroker$Stub",
                "com.google.android.gms.common.api.internal.IGmsServiceBroker$Stub"
            };
            for (String stubName : possibleStubs) {
                try {
                    Class<?> stubClass = Class.forName(stubName);
                    Method asInterfaceMethod = stubClass.getMethod("asInterface", IBinder.class);
                    return asInterfaceMethod.invoke(null, binder);
                } catch (ClassNotFoundException ignored) {}
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("gms");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getService")
    public static class GetService extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && "com.google.android.gms".equals(args[0])) {
                args[0] = BlackBoxCore.getHostPkg();
                Slog.d(TAG, "GmsProxy: Rewriting calling package to " + args[0]);
            }
            return method.invoke(who, args);
        }
    }
}
