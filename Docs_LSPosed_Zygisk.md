# BlackBox LSPosed & Zygisk API Usage Guide

This document outlines how to use the "hyper-perfect" LSPosed and Zygisk implementations within the BlackBox virtual container.

## 1. LSPosed (Xposed API)

BlackBox now supports the standard Xposed API (`de.robv.android.xposed`).

### Creating a Module
Your module should implement the `IXposedHookLoadPackage` interface.

```java
package com.example.module;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;

public class MyModule implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.target.app")) return;

        XposedHelpers.findAndHookMethod("com.target.app.MainActivity",
            lpparam.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Hooked onCreate in " + lpparam.packageName);
            }
        });
    }
}
```

### Loading a Module in BlackBox
In your host application, register the module APK:

```java
import top.niunaijun.blackbox.core.lsposed.LSPosedEngine;

// Add module before launching the target app
LSPosedEngine.addModule("/path/to/your/module.apk");
```

---

## 2. Zygisk (Native Specialization)

The Zygisk engine allows for native-level hooks and environment specialization during the app specialized process.

### Creating a Zygisk Module
Implement the `ZygiskEngine.ZygiskModule` interface.

```java
import top.niunaijun.blackbox.core.lsposed.ZygiskEngine;

public class MyZygiskModule implements ZygiskEngine.ZygiskModule {
    @Override
    public void preAppSpecialize(String packageName, String processName) {
        // Run code BEFORE the application object is created
        // Perfect for native library redirection or early environment setup
    }

    @Override
    public void postAppSpecialize(String packageName, String processName) {
        // Run code AFTER the application is launched
    }
}
```

### Registering Zygisk Specialized Hooks
While the current engine handles core specialization automatically, advanced users can interface with `ZygiskEngine.preAppLaunch` and `postAppLaunch` for custom native injection logic.

---

## 3. Stealth Mode
To prevent detection by sensitive apps, you can toggle stealth mode:

```java
import top.niunaijun.blackbox.core.lsposed.LSPosedEngine;

LSPosedEngine.setStealthMode(true); // Hides hooks from apps matching 'detect' or 'security' patterns
```

This API is designed for commercial-grade stability and is fully compatible with Android 5.0 through 16.0.
