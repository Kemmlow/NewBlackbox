package top.niunaijun.blackbox.core.lsposed;

import android.content.pm.ApplicationInfo;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.niunaijun.blackbox.utils.Slog;

public class LSPosedEngine {
    private static final String TAG = "LSPosedEngine";
    private static final List<IXposedHookLoadPackage> sModules = new ArrayList<>();
    private static boolean sIsHiding = true;

    public static void init() {
        Slog.d(TAG, "Initializing LSPosed engine with stealth mode enabled");
    }

    public static void setStealthMode(boolean enabled) {
        sIsHiding = enabled;
    }

    public static void addModule(String apkPath) {
        try {
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) return;
            PathClassLoader moduleClassLoader = new PathClassLoader(apkPath, LSPosedEngine.class.getClassLoader());
            String entryClassName = "top.niunaijun.blackbox.lsposed.ModuleEntryPoint";
            Class<?> moduleClass = moduleClassLoader.loadClass(entryClassName);
            if (IXposedHookLoadPackage.class.isAssignableFrom(moduleClass)) {
                IXposedHookLoadPackage module = (IXposedHookLoadPackage) moduleClass.newInstance();
                synchronized (sModules) {
                    sModules.add(module);
                }
                Slog.d(TAG, "Successfully loaded module: " + entryClassName);
            }
        } catch (Throwable t) {
            Slog.e(TAG, "Failed to load LSPosed module", t);
        }
    }

    public static void loadModules(String packageName, String processName, ClassLoader classLoader, ApplicationInfo appInfo) {
        if (sIsHiding && (packageName.contains("detect") || packageName.contains("security"))) {
            Slog.d(TAG, "LSPosed hiding from sensitive app: " + packageName);
            return;
        }

        XC_LoadPackage.LoadPackageParam param = new XC_LoadPackage.LoadPackageParam();
        param.packageName = packageName;
        param.processName = processName;
        param.classLoader = classLoader;
        param.appInfo = appInfo;
        param.isFirstApplication = true;

        synchronized (sModules) {
            for (IXposedHookLoadPackage module : sModules) {
                try {
                    module.handleLoadPackage(param);
                } catch (Throwable t) {
                    Slog.e(TAG, "Module error", t);
                }
            }
        }
    }
}
