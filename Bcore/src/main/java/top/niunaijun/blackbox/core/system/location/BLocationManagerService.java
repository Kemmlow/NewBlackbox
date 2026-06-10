package top.niunaijun.blackbox.core.system.location;

import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.Parcel;
import android.util.ArrayMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import black.android.location.BRILocationListener;
import black.android.location.BRILocationListenerStub;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.entity.location.BCell;
import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackbox.fake.frameworks.BLocationManager;
import top.niunaijun.blackbox.utils.CloseUtils;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.Slog;

public class BLocationManagerService extends IBLocationManagerService.Stub {
    private static final String TAG = "BLocationManagerService";
    private static final BLocationManagerService sService = new BLocationManagerService();
    private final Map<Integer, HashMap<String, BLocationConfig>> mLocationConfigs = new HashMap<>();
    private final BLocationConfig mGlobalConfig = new BLocationConfig();
    private final Map<IBinder, LocationRecord> mLocationListeners = new HashMap<>();
    private final Executor mThreadPool = Executors.newCachedThreadPool();

    public static BLocationManagerService get() {
        return sService;
    }

    private BLocationConfig getOrCreateConfig(int userId, String pkg) {
        synchronized (mLocationConfigs) {
            HashMap<String, BLocationConfig> pkgs = mLocationConfigs.get(userId);
            if (pkgs == null) {
                pkgs = new HashMap<>();
                mLocationConfigs.put(userId, pkgs);
            }
            BLocationConfig config = pkgs.get(pkg);
            if (config == null) {
                config = new BLocationConfig();
                config.pattern = BLocationManager.CLOSE_MODE;
                pkgs.put(pkg, config);
            }
            return config;
        }
    }

    @Override
    public int getPattern(int userId, String pkg) {
        synchronized (mLocationConfigs) {
            return getOrCreateConfig(userId, pkg).pattern;
        }
    }

    @Override
    public void setPattern(int userId, String pkg, int pattern) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).pattern = pattern;
            notifyUpdate(userId, pkg);
        }
    }

    @Override
    public void setLocation(int userId, String pkg, BLocation location) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).location = location;
            notifyUpdate(userId, pkg);
        }
    }

    private void notifyUpdate(int userId, String pkg) {
        synchronized (mLocationListeners) {
            for (Map.Entry<IBinder, LocationRecord> entry : mLocationListeners.entrySet()) {
                LocationRecord record = entry.getValue();
                if (record.userId == userId && record.packageName.equals(pkg)) {
                    addTask(entry.getKey());
                }
            }
        }
    }

    @Override
    public BLocation getLocation(int userId, String pkg) {
        BLocationConfig config = getOrCreateConfig(userId, pkg);
        switch (config.pattern) {
            case BLocationManager.OWN_MODE:
                return config.location;
            case BLocationManager.GLOBAL_MODE:
                return mGlobalConfig.location;
            default:
                return null;
        }
    }

    @Override
    public void requestLocationUpdates(IBinder listener, String packageName, int userId) throws RemoteException {
        if (listener == null) return;
        synchronized (mLocationListeners) {
            if (mLocationListeners.containsKey(listener)) return;
            listener.linkToDeath(() -> {
                synchronized (mLocationListeners) {
                    mLocationListeners.remove(listener);
                }
            }, 0);
            mLocationListeners.put(listener, new LocationRecord(packageName, userId));
        }
        addTask(listener);
    }

    @Override
    public void removeUpdates(IBinder listener) throws RemoteException {
        if (listener == null) return;
        synchronized (mLocationListeners) {
            mLocationListeners.remove(listener);
        }
    }

    private void addTask(IBinder locationListener) {
        mThreadPool.execute(() -> {
            try {
                LocationRecord record;
                synchronized (mLocationListeners) {
                    record = mLocationListeners.get(locationListener);
                }
                if (record == null) return;
                BLocation location = getLocation(record.userId, record.packageName);
                if (location != null) {
                    IInterface iInterface = BRILocationListenerStub.get().asInterface(locationListener);
                    if (iInterface != null) {
                        BRILocationListener.get(iInterface).onLocationChanged(location.convert2SystemLocation());
                    }
                }
            } catch (Throwable e) {
                Slog.e(TAG, "addTask error", e);
            }
        });
    }

    @Override public void setCell(int userId, String pkg, BCell cell) {}
    @Override public void setAllCell(int userId, String pkg, List<BCell> cells) {}
    @Override public void setNeighboringCell(int userId, String pkg, List<BCell> cells) {}
    @Override public List<BCell> getNeighboringCell(int userId, String pkg) { return null; }
    @Override public void setGlobalCell(BCell cell) {}
    @Override public void setGlobalAllCell(List<BCell> cells) {}
    @Override public void setGlobalNeighboringCell(List<BCell> cells) {}
    @Override public List<BCell> getGlobalNeighboringCell() { return null; }
    @Override public BCell getCell(int userId, String pkg) { return null; }
    @Override public List<BCell> getAllCell(int userId, String pkg) { return new ArrayList<>(); }
    @Override public void setGlobalLocation(BLocation location) { mGlobalConfig.location = location; }
    @Override public BLocation getGlobalLocation() { return mGlobalConfig.location; }

    private static class LocationRecord {
        String packageName;
        int userId;
        LocationRecord(String packageName, int userId) {
            this.packageName = packageName;
            this.userId = userId;
        }
    }
}
