package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.lang.reflect.Method;

import black.android.webkit.BRWebViewFactory;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;

public class WebViewProxy extends ClassInvocationStub {
    public static final String TAG = "WebViewProxy";

    public WebViewProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        return BRWebViewFactory.get().getProvider();
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        // Handled via reflection in getWho
    }

    @Override
    public boolean isBadEnv() {
        return getBase() == null;
    }

    @ProxyMethod("<init>")
    public static class Constructor extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Slog.d(TAG, "WebView: Constructor called");
            Context context = null;
            if (args != null && args.length > 0 && args[0] instanceof Context) {
                context = (Context) args[0];
            } else {
                context = BlackBoxCore.getContext();
            }

            if (context != null) {
                String userId = String.valueOf(BActivityThread.getUserId());
                String uniqueDataDir = context.getApplicationInfo().dataDir + "/webview_" + userId + "_" + android.os.Process.myPid();

                File dataDir = new File(uniqueDataDir);
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }

                System.setProperty("webview.data.dir", uniqueDataDir);
            }

            Object result = method.invoke(who, args);

            if (result instanceof WebView) {
                configureWebView((WebView) result);
            }

            return result;
        }

        private void configureWebView(WebView webView) {
            try {
                WebSettings settings = webView.getSettings();
                if (settings != null) {
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setDatabaseEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    }
                }
            } catch (Exception e) {
                Slog.w(TAG, "WebView: Failed to configure settings", e);
            }
        }
    }
}
