package de.robv.android.xposed;

import java.lang.reflect.Member;

public abstract class XC_MethodHook {
    public int priority = 50;

    public XC_MethodHook() {}
    public XC_MethodHook(int priority) { this.priority = priority; }

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public static final class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;
        private Object result = null;
        private boolean returnEarly = false;
        public Throwable throwable = null;

        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; this.returnEarly = true; }
        public boolean hasThrowable() { return throwable != null; }
        public Throwable getThrowable() { return throwable; }
        public void setThrowable(Throwable throwable) { this.throwable = throwable; this.returnEarly = true; }
        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) throw throwable;
            return result;
        }
    }

    public class Unhook {
        private final Member hookMethod;
        Unhook(Member hookMethod) { this.hookMethod = hookMethod; }
        public Member getHookedMethod() { return hookMethod; }
        public XC_MethodHook getCallback() { return XC_MethodHook.this; }
    }
}
