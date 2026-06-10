package top.niunaijun.blackbox.core.lsposed;

import top.niunaijun.blackbox.utils.Slog;

public class ZygiskEngine {
    private static final String TAG = "ZygiskEngine";

    static {
        try {
            System.loadLibrary("blackbox_zygisk");
        } catch (Throwable ignored) {}
    }

    public static void preAppLaunch(String packageName, String processName) {
        Slog.d(TAG, "Zygisk pre-launch hook for: " + packageName);
        nativePreAppLaunch(packageName, processName);
    }

    public static void postAppLaunch(String packageName, String processName) {
        Slog.d(TAG, "Zygisk post-launch hook for: " + packageName);
        nativePostAppLaunch(packageName, processName);
    }

    private static native void nativePreAppLaunch(String packageName, String processName);
    private static native void nativePostAppLaunch(String packageName, String processName);

    // Zygisk API for modules
    public interface ZygiskModule {
        void preAppSpecialize(String packageName, String processName);
        void postAppSpecialize(String packageName, String processName);
    }
}
