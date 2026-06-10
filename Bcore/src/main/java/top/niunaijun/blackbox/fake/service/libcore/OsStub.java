package top.niunaijun.blackbox.fake.service.libcore;

import android.os.Process;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.ConnectException;

import black.libcore.io.BRLibcore;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.IOCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Reflector;
import top.niunaijun.blackbox.utils.Slog;

public class OsStub extends ClassInvocationStub {
    public static final String TAG = "OsStub";
    private Object mBase;

    public OsStub() {
        mBase = BRLibcore.get().os();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        BRLibcore.get()._set_os(proxyInvocation);
    }

    @Override
    public boolean isBadEnv() {
        return BRLibcore.get().os() != getProxyInvocation();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String && ((String) args[i]).startsWith("/")) {
                    args[i] = IOCore.get().redirectPath((String) args[i]);
                }
            }
        }
        return super.invoke(proxy, method, args);
    }

    @ProxyMethod("connect")
    public static class Connect extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args.length > 1 && args[1] instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) args[1];
                InetAddress inetAddress = address.getAddress();
                if (inetAddress != null && (inetAddress.isLoopbackAddress() ||
                    inetAddress.getHostAddress().equals("::1") ||
                    inetAddress.getHostAddress().equals("127.0.0.1"))) {
                    Slog.d(TAG, "Loopback isolation: Blocking connection to " + address);
                    throw new ConnectException("Connection refused (sandboxed)");
                }
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getuid")
    public static class getuid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BActivityThread.getBAppId();
        }
    }

    @ProxyMethod("getgid")
    public static class getgid extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BActivityThread.getBAppId();
        }
    }
}
