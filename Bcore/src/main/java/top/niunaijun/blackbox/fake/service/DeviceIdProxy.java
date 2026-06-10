package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;
import android.os.Build;

import black.android.os.BRServiceManager;
import black.android.os.BRIDeviceIdentifiersPolicyServiceStub;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Md5Utils;
import top.niunaijun.blackbox.BlackBoxCore;

public class DeviceIdProxy extends ClassInvocationStub {
    public static final String TAG = "DeviceIdProxy";

    public DeviceIdProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        return BRIDeviceIdentifiersPolicyServiceStub.get().asInterface(BRServiceManager.get().getService("device_identifiers"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
    }

    @Override
    public boolean isBadEnv() {
        return getBase() == null;
    }

    @ProxyMethod("getSerialForPackage")
    public static class GetSerialForPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return Md5Utils.md5(BlackBoxCore.getHostPkg()).substring(0, 16).toUpperCase();
        }
    }

    @ProxyMethod("getImei")
    public static class GetImei extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return "35" + Md5Utils.md5(BlackBoxCore.getHostPkg()).substring(0, 13);
        }
    }
}
