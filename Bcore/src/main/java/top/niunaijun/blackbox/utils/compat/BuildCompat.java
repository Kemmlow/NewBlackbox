package top.niunaijun.blackbox.utils.compat;

import android.os.Build;

public class BuildCompat {
    public static boolean isU() {
        return Build.VERSION.SDK_INT >= 34;
    }
    public static boolean isV() {
        return Build.VERSION.SDK_INT >= 35;
    }
    public static boolean isW() {
        return Build.VERSION.SDK_INT >= 36;
    }
    public static boolean isTiramisu() {
        return Build.VERSION.SDK_INT >= 33;
    }
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= 31;
    }
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= 30;
    }
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= 29;
    }
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= 28;
    }
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= 26;
    }
    public static boolean isN_MR1() {
        return Build.VERSION.SDK_INT >= 25;
    }
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= 24;
    }
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= 23;
    }
    public static boolean isL() {
        return Build.VERSION.SDK_INT >= 21;
    }
    public static boolean isMIUI() {
        return false;
    }
}
