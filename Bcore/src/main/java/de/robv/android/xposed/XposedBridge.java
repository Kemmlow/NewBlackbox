package de.robv.android.xposed;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import top.niunaijun.blackbox.utils.Slog;

public final class XposedBridge {
    private static final String TAG = "XposedBridge";
    private static final Map<Member, XposedBridge.CopyOnWriteSortedSet<XC_MethodHook>> sHookedMethodCallbacks = new HashMap<>();

    public static void log(String text) {
        Slog.d(TAG, text);
    }

    public static void log(Throwable t) {
        Slog.e(TAG, "Xposed log", t);
    }

    public static XC_MethodHook.Unhook hookMethod(Member method, XC_MethodHook callback) {
        synchronized (sHookedMethodCallbacks) {
            CopyOnWriteSortedSet<XC_MethodHook> callbacks = sHookedMethodCallbacks.get(method);
            if (callbacks == null) {
                callbacks = new CopyOnWriteSortedSet<>();
                sHookedMethodCallbacks.put(method, callbacks);
                // Hyper-perfect: native hook initialization would trigger here
            }
            callbacks.add(callback);
        }
        return callback.new Unhook(method);
    }

    public static final class CopyOnWriteSortedSet<T> {
        private final java.util.concurrent.CopyOnWriteArrayList<T> elements = new java.util.concurrent.CopyOnWriteArrayList<>();
        public synchronized boolean add(T element) {
            if (elements.contains(element)) return false;
            return elements.add(element);
        }
        public Object[] getSnapshot() {
            return elements.toArray();
        }
    }
}
